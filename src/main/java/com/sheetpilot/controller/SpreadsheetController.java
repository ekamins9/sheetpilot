package com.sheetpilot.controller;

import com.sheetpilot.dto.SpreadsheetListResponse;
import com.sheetpilot.dto.SpreadsheetPreviewResponse;
import com.sheetpilot.dto.SpreadsheetResponse;
import com.sheetpilot.service.SpreadsheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/spreadsheets")
@RequiredArgsConstructor
public class SpreadsheetController {

    private final SpreadsheetService spreadsheetService;

    /**
     * Upload a new spreadsheet (CSV or XLSX)
     * POST /api/spreadsheets/upload
     *
     * @param file MultipartFile - the CSV or XLSX file to upload
     * @param uploadedBy Optional username of the uploader
     * @return SpreadsheetResponse with metadata about the uploaded spreadsheet
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpreadsheetResponse> uploadSpreadsheet(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {

        log.info("Received spreadsheet upload request: {}", file.getOriginalFilename());
        SpreadsheetResponse response = spreadsheetService.uploadSpreadsheet(file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all spreadsheets with pagination
     * GET /api/spreadsheets?page=0&size=10&sort=uploadedAt,desc
     *
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @param sort Sort field and direction (default: uploadedAt,desc)
     * @return SpreadsheetListResponse with paginated list of spreadsheets
     */
    @GetMapping
    public ResponseEntity<SpreadsheetListResponse> getAllSpreadsheets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt,desc") String[] sort) {

        log.info("Fetching spreadsheets - page: {}, size: {}", page, size);

        // Parse sort parameter
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        SpreadsheetListResponse response = spreadsheetService.getAllSpreadsheets(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get spreadsheet metadata by ID
     * GET /api/spreadsheets/{id}
     *
     * @param id Spreadsheet ID
     * @return SpreadsheetResponse with metadata
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpreadsheetResponse> getSpreadsheetById(@PathVariable Long id) {
        log.info("Fetching spreadsheet with ID: {}", id);
        SpreadsheetResponse response = spreadsheetService.getSpreadsheetById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get preview of spreadsheet data (first 100 rows)
     * GET /api/spreadsheets/{id}/preview
     *
     * Note: This implementation requires re-uploading the file for preview
     * In production, you'd store the file and retrieve it from storage
     *
     * @param id Spreadsheet ID
     * @param file The spreadsheet file (required for preview)
     * @return SpreadsheetPreviewResponse with headers and preview rows
     */
    @GetMapping(value = "/{id}/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpreadsheetPreviewResponse> getSpreadsheetPreview(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        log.info("Generating preview for spreadsheet ID: {}", id);
        SpreadsheetPreviewResponse response = spreadsheetService.getSpreadsheetPreview(id, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a spreadsheet by ID
     * DELETE /api/spreadsheets/{id}
     *
     * @param id Spreadsheet ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpreadsheet(@PathVariable Long id) {
        log.info("Deleting spreadsheet with ID: {}", id);
        spreadsheetService.deleteSpreadsheet(id);
        return ResponseEntity.noContent().build();
    }
}
