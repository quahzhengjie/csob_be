package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;

@Data
public class CallReportDto {
    private String reportId;  // This is what frontend expects, e.g., "CR-001"
    @NotNull
    private Instant callDate;
    @NotBlank
    private String summary;
    private String nextSteps;
}