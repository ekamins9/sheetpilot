package com.sheetpilot.service;

import com.sheetpilot.dto.SpreadsheetListResponse;
import com.sheetpilot.dto.SpreadsheetPreviewResponse;
import com.sheetpilot.dto.SpreadsheetResponse;
import com.sheetpilot.exception.FileStorageException;
import com.sheetpilot.exception.InvalidFileException;
import com.sheetpilot.exception.ResourceNotFoundException;
import com.sheetpilot.model.Spreadsheet;
import com.sheetpilot.repository.SpreadsheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpreadsheetService {

    private final SpreadsheetRepository spreadsheetRepository;
    private static final int PREVIEW_ROW_LIMIT = 1000; // Store first 1000 rows
    private static final List<String> ALLOWED_EXTENSIONS = List.of("csv", "xlsx");
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    @Transactional
    public SpreadsheetResponse uploadSpreadsheet(MultipartFile file, String uploadedBy) {
        validateFile(file);

        String fileName = file.getOriginalFilename();
        String fileType = getFileExtension(fileName);

        log.info("Processing file upload: {} (type: {})", fileName, fileType);

        try {
            log.info("Parsing file: {} (size: {} bytes)", fileName, file.getSize());
            SpreadsheetData data = parseFile(file, fileType);
            log.info("File parsed successfully: {} rows, {} columns", data.getRowCount(), data.getColumnCount());

            // Prepare preview data (first 1000 rows)
            List<List<String>> previewRows = data.getRows().stream()
                    .limit(PREVIEW_ROW_LIMIT)
                    .collect(Collectors.toList());

            Map<String, Object> previewData = new HashMap<>();
            previewData.put("headers", data.getHeaders());
            previewData.put("rows", previewRows);
            previewData.put("totalRows", data.getRowCount());
            previewData.put("previewRows", previewRows.size());

            Spreadsheet spreadsheet = Spreadsheet.builder()
                    .name(fileName)
                    .fileName(fileName)
                    .fileType(fileType)
                    .fileSize(file.getSize())
                    .rowCount(data.getRowCount())
                    .columnCount(data.getColumnCount())
                    .uploadedBy(uploadedBy != null ? uploadedBy : "anonymous")
                    .previewData(previewData)
                    .uploadedAt(java.time.Instant.now())
                    .updatedAt(java.time.Instant.now())
                    .build();

            Spreadsheet saved = spreadsheetRepository.save(spreadsheet);
            log.info("Spreadsheet saved successfully with ID: {} ({} rows stored in preview)",
                    saved.getId(), previewRows.size());

            return mapToResponse(saved);

        } catch (IOException e) {
            log.error("Error processing file: {}", fileName, e);
            throw new FileStorageException("Failed to store file: " + fileName, e);
        } catch (Exception e) {
            log.error("Unexpected error processing file: {}", fileName, e);
            throw new FileStorageException("Unexpected error processing file: " + fileName, e);
        }
    }

    @Transactional(readOnly = true)
    public SpreadsheetListResponse getAllSpreadsheets(Pageable pageable) {
        Page<Spreadsheet> page = spreadsheetRepository.findAll(pageable);

        List<SpreadsheetResponse> spreadsheets = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return SpreadsheetListResponse.builder()
                .spreadsheets(spreadsheets)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public SpreadsheetResponse getSpreadsheetById(Long id) {
        Spreadsheet spreadsheet = spreadsheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spreadsheet", id));
        return mapToResponse(spreadsheet);
    }

    @Transactional(readOnly = true)
    public SpreadsheetPreviewResponse getSpreadsheetPreview(Long id) {
        log.info("Fetching preview for spreadsheet ID: {}", id);

        Spreadsheet spreadsheet = spreadsheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spreadsheet", id));

        // Retrieve stored preview data from database
        Map<String, Object> previewData = spreadsheet.getPreviewData();

        if (previewData == null || previewData.isEmpty()) {
            log.warn("No preview data found for spreadsheet ID: {}", id);
            throw new InvalidFileException("Preview data not available for this spreadsheet");
        }

        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) previewData.get("headers");

        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) previewData.get("rows");

        Integer totalRows = (Integer) previewData.getOrDefault("totalRows", spreadsheet.getRowCount());
        Integer previewRows = (Integer) previewData.getOrDefault("previewRows", rows != null ? rows.size() : 0);

        log.info("Retrieved preview for spreadsheet ID: {} ({} rows)", id, previewRows);

        return SpreadsheetPreviewResponse.builder()
                .spreadsheetId(spreadsheet.getId())
                .name(spreadsheet.getName())
                .headers(headers != null ? headers : new ArrayList<>())
                .rows(rows != null ? rows : new ArrayList<>())
                .totalRows(totalRows)
                .previewRows(previewRows)
                .build();
    }

    @Transactional
    public void deleteSpreadsheet(Long id) {
        if (!spreadsheetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spreadsheet", id);
        }
        spreadsheetRepository.deleteById(id);
        log.info("Deleted spreadsheet with ID: {}", id);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new InvalidFileException("File name is invalid");
        }

        String extension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("Unsupported file type: %s. Allowed types: %s",
                            extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }

        // Check file size (max 50MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size ({} bytes) exceeds maximum allowed size ({} bytes)",
                    file.getSize(), MAX_FILE_SIZE);
            throw new InvalidFileException("File size exceeds maximum allowed size of 50MB");
        }

        // Check for empty content
        if (file.getSize() == 0) {
            log.warn("Empty file detected: {}", fileName);
            throw new InvalidFileException("File is empty");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new InvalidFileException("File has no extension");
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private SpreadsheetData parseFile(MultipartFile file, String fileType) throws IOException {
        return switch (fileType.toLowerCase()) {
            case "csv" -> parseCsvFile(file);
            case "xlsx" -> parseExcelFile(file);
            default -> throw new InvalidFileException("Unsupported file type: " + fileType);
        };
    }

    private SpreadsheetData parseCsvFile(MultipartFile file) throws IOException {
        log.debug("Parsing CSV file: {}", file.getOriginalFilename());

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines()
                     .withTrim())) {

            List<String> headers = new ArrayList<>(csvParser.getHeaderNames());

            if (headers.isEmpty()) {
                log.error("CSV file has no headers: {}", file.getOriginalFilename());
                throw new InvalidFileException("CSV file must have header row");
            }

            List<List<String>> rows = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.add(record.get(i));
                }
                rows.add(row);
            }

            log.debug("CSV parsed: {} headers, {} rows", headers.size(), rows.size());
            return new SpreadsheetData(headers, rows);

        } catch (IllegalArgumentException e) {
            log.error("Malformed CSV file: {}", file.getOriginalFilename(), e);
            throw new InvalidFileException("Malformed CSV file: " + e.getMessage(), e);
        }
    }

    private SpreadsheetData parseExcelFile(MultipartFile file) throws IOException {
        log.debug("Parsing Excel file: {}", file.getOriginalFilename());

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                log.error("Excel file has no sheets: {}", file.getOriginalFilename());
                throw new InvalidFileException("Excel file must contain at least one sheet");
            }

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            log.debug("Processing sheet: {}", sheet.getSheetName());

            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            // Read headers from first row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || headerRow.getPhysicalNumberOfCells() == 0) {
                log.error("Excel file has no header row: {}", file.getOriginalFilename());
                throw new InvalidFileException("Excel file must have header row");
            }

            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    List<String> rowData = new ArrayList<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        rowData.add(getCellValueAsString(cell));
                    }
                    rows.add(rowData);
                }
            }

            log.debug("Excel parsed: {} headers, {} rows", headers.size(), rows.size());
            return new SpreadsheetData(headers, rows);

        } catch (IllegalArgumentException e) {
            log.error("Invalid Excel file format: {}", file.getOriginalFilename(), e);
            throw new InvalidFileException("Invalid Excel file: " + e.getMessage(), e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    private SpreadsheetResponse mapToResponse(Spreadsheet spreadsheet) {
        return SpreadsheetResponse.builder()
                .id(spreadsheet.getId())
                .name(spreadsheet.getName())
                .fileType(spreadsheet.getFileType())
                .fileSize(spreadsheet.getFileSize())
                .rowCount(spreadsheet.getRowCount())
                .columnCount(spreadsheet.getColumnCount())
                .uploadedBy(spreadsheet.getUploadedBy())
                .uploadedAt(spreadsheet.getUploadedAt())
                .updatedAt(spreadsheet.getUpdatedAt())
                .build();
    }

    // Inner class to hold parsed spreadsheet data
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SpreadsheetData {
        private List<String> headers;
        private List<List<String>> rows;

        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return headers.size();
        }
    }
}
