package com.sheetpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
    private JobResultDTO result;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class JobResultDTO {
    private Integer beforeRowCount;
    private Integer afterRowCount;
    private Integer beforeColumnCount;
    private Integer afterColumnCount;
    private List<String> columnsAdded;
    private List<String> columnsRemoved;
    private Map<String, String> columnsRenamed;
    private Integer nullValuesRemoved;
    private Integer duplicatesRemoved;
    private Integer recordsFiltered;
    private Double dataQualityScore;
    private List<String> warnings;
    private ResultPreviewDTO resultPreview;
    private String downloadUrl;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResultPreviewDTO {
    private List<String> headers;
    private List<List<String>> rows;
}
