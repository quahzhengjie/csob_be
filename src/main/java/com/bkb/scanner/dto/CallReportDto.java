package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * DTO for CallReport
 * All timestamps are in UTC and will be serialized as ISO-8601
 */
@Data
public class CallReportDto {
    private String reportId;  // Frontend formatted ID like "CR-001"

    @NotNull
    private Instant callDate;  // ISO-8601 UTC timestamp

    @NotBlank
    private String summary;

    private String nextSteps;

    // Enhanced fields
    private String callType; // 'Inbound', 'Outbound', 'Meeting', 'Email'
    private Integer duration; // in minutes
    private String outcome; // 'Positive', 'Neutral', 'Negative', 'Follow-up Required'
    private List<String> attendees;

    // Audit fields (from Auditable)
    private String createdBy;
    private Instant createdDate;

    // Note: We typically don't send soft delete fields to frontend
    // unless specifically requested (e.g., in an audit view)
}