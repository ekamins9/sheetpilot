package com.sheetpilot.service;

import com.sheetpilot.dto.JobRequest;
import com.sheetpilot.dto.JobResponse;
import com.sheetpilot.dto.JobStepStatusDTO;
import com.sheetpilot.exception.ResourceNotFoundException;
import com.sheetpilot.model.Pipeline;
import com.sheetpilot.model.PipelineStep;
import com.sheetpilot.model.Spreadsheet;
import com.sheetpilot.model.TransformationJob;
import com.sheetpilot.repository.PipelineRepository;
import com.sheetpilot.repository.SpreadsheetRepository;
import com.sheetpilot.repository.TransformationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final TransformationJobRepository jobRepository;
    private final PipelineRepository pipelineRepository;
    private final SpreadsheetRepository spreadsheetRepository;

    @Transactional
    public JobResponse createJob(JobRequest request) {
        Pipeline pipeline = pipelineRepository.findById(request.getPipelineId())
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline not found"));

        // Validate spreadsheets exist
        List<Spreadsheet> spreadsheets = spreadsheetRepository.findAllById(request.getSpreadsheetIds());
        if (spreadsheets.size() != request.getSpreadsheetIds().size()) {
            throw new ResourceNotFoundException("One or more spreadsheets not found");
        }

        // Initialize step statuses
        List<Map<String, Object>> stepStatuses = new ArrayList<>();
        for (int i = 0; i < pipeline.getSteps().size(); i++) {
            PipelineStep step = pipeline.getSteps().get(i);
            Map<String, Object> stepStatus = new HashMap<>();
            stepStatus.put("stepOrder", i);
            stepStatus.put("stepName", step.getTransformationType());
            stepStatus.put("status", "pending");
            stepStatus.put("startedAt", null);
            stepStatus.put("completedAt", null);
            stepStatus.put("errorMessage", null);
            stepStatus.put("rowsProcessed", 0);
            stepStatuses.add(stepStatus);
        }

        // Calculate estimated cost (simple calculation: $0.001 per 1000 rows)
        long totalRows = spreadsheets.stream()
                .mapToLong(Spreadsheet::getRowCount)
                .sum();
        double estimatedCost = (totalRows / 1000.0) * 0.001;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("steps", stepStatuses);
        metadata.put("logs", new ArrayList<String>());
        metadata.put("estimatedCost", estimatedCost);
        metadata.put("actualCost", null);

        TransformationJob job = TransformationJob.builder()
                .pipeline(pipeline)
                .spreadsheetIds(request.getSpreadsheetIds())
                .status(TransformationJob.JobStatus.PENDING)
                .progress(0.0)
                .metadata(metadata)
                .build();

        job = jobRepository.save(job);

        // In a real application, you would trigger async processing here
        // For now, we'll just return the created job
        // TODO: Implement async job processing

        return mapToResponse(job, pipeline, spreadsheets);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {
        TransformationJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Pipeline pipeline = job.getPipeline();
        List<Spreadsheet> spreadsheets = spreadsheetRepository.findAllById(job.getSpreadsheetIds());

        return mapToResponse(job, pipeline, spreadsheets);
    }

    @Transactional
    public JobResponse cancelJob(Long id) {
        TransformationJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != TransformationJob.JobStatus.PENDING &&
            job.getStatus() != TransformationJob.JobStatus.RUNNING) {
            throw new IllegalStateException("Cannot cancel job in " + job.getStatus() + " status");
        }

        job.setStatus(TransformationJob.JobStatus.CANCELLED);
        job.setCompletedAt(Instant.now());
        job = jobRepository.save(job);

        Pipeline pipeline = job.getPipeline();
        List<Spreadsheet> spreadsheets = spreadsheetRepository.findAllById(job.getSpreadsheetIds());

        return mapToResponse(job, pipeline, spreadsheets);
    }

    @SuppressWarnings("unchecked")
    private JobResponse mapToResponse(TransformationJob job, Pipeline pipeline, List<Spreadsheet> spreadsheets) {
        Map<String, Object> metadata = job.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        List<Map<String, Object>> stepStatusMaps = (List<Map<String, Object>>) metadata.getOrDefault("steps", new ArrayList<>());
        List<JobStepStatusDTO> steps = stepStatusMaps.stream()
                .map(this::mapStepStatus)
                .collect(Collectors.toList());

        List<String> logs = (List<String>) metadata.getOrDefault("logs", new ArrayList<>());
        Double estimatedCost = ((Number) metadata.getOrDefault("estimatedCost", 0.0)).doubleValue();
        Object actualCostObj = metadata.get("actualCost");
        Double actualCost = actualCostObj != null ? ((Number) actualCostObj).doubleValue() : null;

        long executionTimeMs = 0;
        if (job.getStartedAt() != null) {
            Instant endTime = job.getCompletedAt() != null ? job.getCompletedAt() : Instant.now();
            executionTimeMs = endTime.toEpochMilli() - job.getStartedAt().toEpochMilli();
        }

        return JobResponse.builder()
                .id(job.getId())
                .pipelineId(pipeline.getId())
                .pipelineName(pipeline.getName())
                .spreadsheetIds(job.getSpreadsheetIds())
                .spreadsheetNames(spreadsheets.stream().map(Spreadsheet::getName).collect(Collectors.toList()))
                .status(job.getStatus().name().toLowerCase())
                .progress(job.getProgress())
                .startedAt(job.getStartedAt() != null ? job.getStartedAt() : job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .executionTimeMs(executionTimeMs)
                .steps(steps)
                .logs(logs)
                .errorMessage(job.getErrorMessage())
                .estimatedCost(estimatedCost)
                .actualCost(actualCost)
                .build();
    }

    private JobStepStatusDTO mapStepStatus(Map<String, Object> stepMap) {
        return JobStepStatusDTO.builder()
                .stepOrder(((Number) stepMap.get("stepOrder")).intValue())
                .stepName((String) stepMap.get("stepName"))
                .status((String) stepMap.get("status"))
                .startedAt(stepMap.get("startedAt") != null ? Instant.parse((String) stepMap.get("startedAt")) : null)
                .completedAt(stepMap.get("completedAt") != null ? Instant.parse((String) stepMap.get("completedAt")) : null)
                .errorMessage((String) stepMap.get("errorMessage"))
                .rowsProcessed(((Number) stepMap.getOrDefault("rowsProcessed", 0)).intValue())
                .build();
    }

    @Transactional(readOnly = true)
    public Resource downloadResult(Long jobId, String format) {
        TransformationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != TransformationJob.JobStatus.COMPLETED) {
            throw new IllegalStateException("Job is not completed");
        }

        // TODO: Generate actual CSV/XLSX from result data
        // For now, return a simple CSV with mock data
        String csv = "id,name,value\n1,Sample,100\n2,Data,200\n";
        return new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public Long saveResultAsSpreadsheet(Long jobId) {
        TransformationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != TransformationJob.JobStatus.COMPLETED) {
            throw new IllegalStateException("Job is not completed");
        }

        // TODO: Create actual spreadsheet from result data
        // For now, return a mock ID
        return 999L;
    }
}
