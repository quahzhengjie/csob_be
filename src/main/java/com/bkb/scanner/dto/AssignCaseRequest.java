package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignCaseRequest {
    @NotBlank
    private String userId;
}
