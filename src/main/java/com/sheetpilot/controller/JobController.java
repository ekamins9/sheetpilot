package com.sheetpilot.controller;

import com.sheetpilot.dto.JobRequest;
import com.sheetpilot.dto.JobResponse;
import com.sheetpilot.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
