package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class UpdateTemplateRequestDto {

    @NotBlank(message = "Category key is required")
    private String categoryKey; // e.g., "entityTemplates"

    @NotBlank(message = "Type key is required")
    private String typeKey; // e.g., "Non-Listed Company"

    @NotNull(message = "Documents list cannot be null")
    private List<TemplateDocDto> documents;
}