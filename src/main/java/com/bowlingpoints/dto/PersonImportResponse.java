package com.bowlingpoints.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PersonImportResponse {
    private int successCount;
    private int errorCount;
    private int totalProcessed;
    private List<String> errors;
}