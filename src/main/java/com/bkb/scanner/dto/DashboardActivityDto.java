
// Dashboard Activity DTO
package com.bkb.scanner.dto;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DashboardActivityDto {
    private List<CaseActivityItem> recentCases;
    private List<CaseActivityItem> urgentCases;

    @Data
    @Builder
    public static class CaseActivityItem {
        private String caseId;
        private String entityName;
        private String entityType;
        private String status;
        private String riskLevel;
        private Instant createdDate;
        private Instant slaDeadline;
        private Integer daysUntilDeadline;
        private DeadlineStatus deadlineStatus;
    }

    public enum DeadlineStatus {
        OVERDUE, URGENT, NORMAL
    }
}