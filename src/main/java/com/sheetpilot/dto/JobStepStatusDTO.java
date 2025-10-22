package com.sheetpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStepStatusDTO {
    private Integer stepOrder;
    private String stepName;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;
    private Integer rowsProcessed;
}
