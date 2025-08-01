package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseUpdateRequest {
    @NotBlank
    private String status;
    @NotBlank
    private String riskLevel;
}
