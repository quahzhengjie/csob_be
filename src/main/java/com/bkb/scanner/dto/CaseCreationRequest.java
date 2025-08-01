
// FILE: src/main/java/com/bkb/scanner/dto/CaseCreationRequest.java
package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CaseCreationRequest {
    @NotBlank
    private String entityName;
    @NotBlank
    private String entityType;
    @NotNull
    private String riskLevel;
    @NotNull
    private String status;
}