// src/main/java/com/bkb/scanner/dto/ChecklistDto.java
package com.bkb.scanner.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChecklistDto {
    private List<ChecklistSectionDto> checklist;
    private ProgressDto progress;

    @Data
    @Builder
    public static class ProgressDto {
        private int percentage;
        private List<ChecklistDocumentDto> missingDocs;
        private List<ChecklistDocumentDto> expiringDocs;
    }
}