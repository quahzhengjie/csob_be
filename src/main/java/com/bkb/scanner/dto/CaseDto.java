package com.bkb.scanner.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * DTO for representing a case with ALL fields required by the frontend.
 */
@Data
public class CaseDto {
    private String caseId;
    private String status;
    private String riskLevel;
    private Instant createdDate;
    private Instant slaDeadline;
    private String assignedTo;        // User ID of assigned user
    private String approvedBy;        // User ID of approver
    private String workflowStage;
    private List<String> approvalChain;

    // The nested entity data
    private EntityDataDto entity;

    // Related parties with their relationships
    private List<RelatedPartyLinkDto> relatedPartyLinks;

    // Additional data
    private List<CallReportDto> callReports;
    private List<ActivityLogDto> activities;

    // Removed these as frontend doesn't use them at the case list level
    // private Instant lastModifiedDate;
    // private List<DocumentDto> documents;
}