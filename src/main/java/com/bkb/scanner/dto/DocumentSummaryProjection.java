package com.bkb.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Projection class for efficient batch loading of document summaries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryProjection {
    private String caseId;
    private long expiredCount;
    private long expiringSoonCount;
    private long verifiedCount;
    private long submittedCount;
    private long rejectedCount;

    // Constructor for JPQL query projection
    public DocumentSummaryProjection(String caseId, long expiredCount, long expiringSoonCount) {
        this.caseId = caseId;
        this.expiredCount = expiredCount;
        this.expiringSoonCount = expiringSoonCount;
        this.verifiedCount = 0L;
        this.submittedCount = 0L;
        this.rejectedCount = 0L;
    }
}