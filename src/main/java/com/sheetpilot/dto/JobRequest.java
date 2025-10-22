package com.sheetpilot.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobRequest {
    private Long pipelineId;
    private List<Long> spreadsheetIds;
}
