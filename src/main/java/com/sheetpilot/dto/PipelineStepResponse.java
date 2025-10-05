package com.sheetpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepResponse {
    private Long id;
    private Integer stepOrder;
    private String transformationType;
    private Map<String, Object> config;
}
