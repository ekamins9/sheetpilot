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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpreadsheetService {

    private final SpreadsheetRepository spreadsheetRepository;
    private static final int PREVIEW_ROW_LIMIT = 100;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("csv", "xlsx");

    @Transactional
    public SpreadsheetResponse uploadSpreadsheet(MultipartFile file, String uploadedBy) {
        validateFile(file);

        String fileName = file.getOriginalFilename();
        String fileType = getFileExtension(fileName);

        log.info("Processing file upload: {} (type: {})", fileName, fileType);

        try {
            SpreadsheetData data = parseFile(file, fileType);

            Spreadsheet spreadsheet = Spreadsheet.builder()
                    .name(fileName)
                    .fileType(fileType)
                    .fileSize(file.getSize())
                    .rowCount(data.getRowCount())
                    .columnCount(data.getColumnCount())
                    .uploadedBy(uploadedBy != null ? uploadedBy : "anonymous")
                    .uploadedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Store the file data (for now, we'll just store metadata)
            // In a production system, you'd store the actual file content in a blob storage
            // or store the parsed data in a separate table

            Spreadsheet saved = spreadsheetRepository.save(spreadsheet);
            log.info("Spreadsheet saved with ID: {}", saved.getId());

            return mapToResponse(saved);

        } catch (IOException e) {
            log.error("Error processing file: {}", fileName, e);
            throw new FileStorageException("Failed to store file: " + fileName, e);
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
    public SpreadsheetPreviewResponse getSpreadsheetPreview(Long id, MultipartFile file) {
        Spreadsheet spreadsheet = spreadsheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spreadsheet", id));

        // For this implementation, we'll require the file to be uploaded again for preview
        // In production, you'd retrieve from storage
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required for preview");
        }

        try {
            SpreadsheetData data = parseFile(file, spreadsheet.getFileType());
            List<List<String>> previewRows = data.getRows().stream()
                    .limit(PREVIEW_ROW_LIMIT)
                    .collect(Collectors.toList());

            return SpreadsheetPreviewResponse.builder()
                    .spreadsheetId(spreadsheet.getId())
                    .name(spreadsheet.getName())
                    .headers(data.getHeaders())
                    .rows(previewRows)
                    .totalRows(data.getRowCount())
                    .previewRows(previewRows.size())
                    .build();

        } catch (IOException e) {
            log.error("Error generating preview for spreadsheet ID: {}", id, e);
            throw new FileStorageException("Failed to generate preview", e);
        }
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

        // Check file size (e.g., max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File size exceeds maximum allowed size of 10MB");
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
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> headers = new ArrayList<>(csvParser.getHeaderNames());
            List<List<String>> rows = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.add(record.get(i));
                }
                rows.add(row);
            }

            return new SpreadsheetData(headers, rows);
        }
    }

    private SpreadsheetData parseExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet

            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            // Read headers from first row
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
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

            return new SpreadsheetData(headers, rows);
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
