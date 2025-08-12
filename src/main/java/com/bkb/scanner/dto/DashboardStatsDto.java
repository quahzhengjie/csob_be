package com.bkb.scanner.dto;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DashboardStatsDto {
    private OverviewStats overview;
    private TrendStats trends;
    private RiskDistribution riskDistribution;
    private List<EntityTypeDistribution> entityTypeDistribution;

    @Data
    @Builder
    public static class OverviewStats {
        private Long totalCases;
        private Long inProgress;
        private Long pendingApproval;
        private Long active;
        private Long rejected;
        private Long overdue;
    }

    @Data
    @Builder
    public static class TrendStats {
        private Long casesThisWeek;
        private Double weeklyGrowth;
        private Double avgProcessingDays;
    }

    @Data
    @Builder
    public static class RiskDistribution {
        private Long high;
        private Long medium;
        private Long low;
    }

    @Data
    @Builder
    public static class EntityTypeDistribution {
        private String type;
        private Long count;
    }
}