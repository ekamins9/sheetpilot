package com.sheetpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private Long pipelineId;
    private String pipelineName;
    private List<Long> spreadsheetIds;
    private List<String> spreadsheetNames;
    private String status;
    private Double progress;
    private Instant startedAt;
    private Instant completedAt;
    private Long executionTimeMs;
    private List<JobStepStatusDTO> steps;
    private List<String> logs;
    private String errorMessage;
    private Double estimatedCost;
    private Double actualCost;
}
