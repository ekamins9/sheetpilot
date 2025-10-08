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
public class SpreadsheetResponse {
    private Long id;
    private String name;
    private String fileType;
    private Long fileSize;
    private Integer rowCount;
    private Integer columnCount;
    private String uploadedBy;
    private Instant uploadedAt;
    private Instant updatedAt;
}
