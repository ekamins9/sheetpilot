package com.sheetpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpreadsheetListResponse {
    private List<SpreadsheetResponse> spreadsheets;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
