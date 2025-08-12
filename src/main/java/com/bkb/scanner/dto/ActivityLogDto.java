package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.Instant;

@Data
public class ActivityLogDto {
    private String activityId;  // e.g., "ACT-001"
    @NotBlank
    private String type;
    @NotBlank
    private String details;
    private String performedBy;  // User who performed the action
    private Instant timestamp;   // When the action occurred
}