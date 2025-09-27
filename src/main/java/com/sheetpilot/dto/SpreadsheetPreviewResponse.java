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
public class SpreadsheetPreviewResponse {
    private Long spreadsheetId;
    private String name;
    private List<String> headers;
    private List<List<String>> rows;
    private int totalRows;
    private int previewRows;
}
