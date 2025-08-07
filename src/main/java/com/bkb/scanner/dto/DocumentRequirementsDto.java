package com.bkb.scanner.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DTO for sending the complete, unified document requirement structure to the frontend.
 */
@Data
public class DocumentRequirementsDto {

    private Map<String, List<TemplateDocDto>> entityTemplates;
    private Map<String, List<TemplateDocDto>> individualTemplates;
    private Map<String, List<TemplateDocDto>> riskBasedDocuments;

    // This field holds the data for the entity roles.
    private Map<String, List<String>> entityRoleMapping;
}