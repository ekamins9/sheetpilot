package com.sheetpilot.controller;

import com.sheetpilot.dto.JobRequest;
import com.sheetpilot.dto.JobResponse;
import com.sheetpilot.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        JobResponse job = jobService.createJob(request);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        JobResponse job = jobService.getJob(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobResponse> cancelJob(@PathVariable Long id) {
        JobResponse job = jobService.cancelJob(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResult(
            @PathVariable Long id,
            @RequestParam(defaultValue = "csv") String format) {
        Resource resource = jobService.downloadResult(id, format);

        String filename = "job-" + id + "-result." + format;
        String contentType = format.equals("xlsx")
            ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            : "text/csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/save-result")
    public ResponseEntity<Map<String, Long>> saveResultAsSpreadsheet(@PathVariable Long id) {
        Long spreadsheetId = jobService.saveResultAsSpreadsheet(id);
        return ResponseEntity.ok(Map.of("spreadsheetId", spreadsheetId));
    }
}
