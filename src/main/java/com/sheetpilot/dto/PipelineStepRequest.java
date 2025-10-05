package com.sheetpilot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepRequest {

    @NotNull(message = "Step order is required")
    @Min(value = 0, message = "Step order must be non-negative")
    private Integer stepOrder;

    @NotBlank(message = "Transformation type is required")
    private String transformationType;

    private Map<String, Object> config;
}
