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
public class PipelineResponse {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PipelineStepResponse> steps;
}
