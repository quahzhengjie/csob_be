// src/main/java/com/bkb/scanner/dto/ChecklistSectionDto.java
package com.bkb.scanner.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChecklistSectionDto {
    private String category;
    private List<ChecklistDocumentDto> documents;
}