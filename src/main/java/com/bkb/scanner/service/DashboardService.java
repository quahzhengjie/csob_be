package com.bkb.scanner.service;

import com.bkb.scanner.dto.DashboardActivityDto;
import com.bkb.scanner.dto.DashboardActivityDto.CaseActivityItem;
import com.bkb.scanner.dto.DashboardActivityDto.DeadlineStatus;
import com.bkb.scanner.dto.DashboardStatsDto;
import com.bkb.scanner.entity.Case;
import com.bkb.scanner.entity.ActivityLog;
import com.bkb.scanner.repository.CaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private CaseRepository caseRepository;

    // Define constants for statuses
    private static final List<String> EXCLUDED_STATUSES = Arrays.asList("Active", "Rejected");
    private static final List<String> COMPLETED_STATUSES = Arrays.asList("Active", "Rejected");

    @Cacheable(value = "dashboardStats", key = "#timeFilter", unless = "#result == null")
    public DashboardStatsDto getDashboardStats(String timeFilter) {
        log.debug("Fetching dashboard stats for timeFilter: {}", timeFilter);

        Instant now = Instant.now();

        // Overview Stats - using database-agnostic queries
        DashboardStatsDto.OverviewStats overview = DashboardStatsDto.OverviewStats.builder()
                .totalCases(caseRepository.count())
                .inProgress(caseRepository.countByStatus("KYC Review"))
                .pendingApproval(caseRepository.countByStatus("Pending Approval"))
                .active(caseRepository.countByStatus("Active"))
                .rejected(caseRepository.countByStatus("Rejected"))
                .overdue(caseRepository.countOverdueCases(now, EXCLUDED_STATUSES))
                .build();

        // Trend Stats
        Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);

        Long casesThisWeek = caseRepository.countByCreatedDateAfter(oneWeekAgo);
        Long casesLastWeek = caseRepository.countByCreatedDateBetween(twoWeeksAgo, oneWeekAgo);

        double weeklyGrowth = calculateGrowthPercentage(casesThisWeek, casesLastWeek);

        // Calculate average processing days in Java (database agnostic)
        Double avgProcessingDays = calculateAverageProcessingDays();

        DashboardStatsDto.TrendStats trends = DashboardStatsDto.TrendStats.builder()
                .casesThisWeek(casesThisWeek)
                .weeklyGrowth(Math.round(weeklyGrowth * 100.0) / 100.0)
                .avgProcessingDays(avgProcessingDays)
                .build();

        // Risk Distribution
        DashboardStatsDto.RiskDistribution riskDistribution = DashboardStatsDto.RiskDistribution.builder()
                .high(caseRepository.countByRiskLevel("High"))
                .medium(caseRepository.countByRiskLevel("Medium"))
                .low(caseRepository.countByRiskLevel("Low"))
                .build();

        // Entity Type Distribution - using interface projection
        List<DashboardStatsDto.EntityTypeDistribution> entityTypeDistribution =
                caseRepository.getEntityTypeDistribution().stream()
                        .limit(4) // Top 4 entity types
                        .map(row -> DashboardStatsDto.EntityTypeDistribution.builder()
                                .type(row.getEntityType() != null ? row.getEntityType() : "Unknown")
                                .count(row.getCount())
                                .build())
                        .collect(Collectors.toList());

        return DashboardStatsDto.builder()
                .overview(overview)
                .trends(trends)
                .riskDistribution(riskDistribution)
                .entityTypeDistribution(entityTypeDistribution)
                .build();
    }

    private double calculateGrowthPercentage(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double)(current - previous) / previous) * 100;
    }

    // Database-agnostic calculation of average processing days
    private Double calculateAverageProcessingDays() {
        try {
            List<Case> completedCases = caseRepository.findCompletedCasesWithActivityLogs(COMPLETED_STATUSES);

            if (completedCases.isEmpty()) {
                return 0.0;
            }

            double totalDays = 0;
            int count = 0;

            for (Case caseEntity : completedCases) {
                // Find the most recent approval/rejection activity
                ActivityLog completionActivity = caseEntity.getActivityLogs().stream()
                        .filter(a -> "CASE_APPROVED".equals(a.getType()) ||
                                "CASE_REJECTED".equals(a.getType()) ||
                                "status_changed".equals(a.getType())) // Handle different activity types
                        .max((a1, a2) -> a1.getCreatedDate().compareTo(a2.getCreatedDate()))
                        .orElse(null);

                if (completionActivity != null) {
                    Duration duration = Duration.between(
                            caseEntity.getCreatedDate(),
                            completionActivity.getCreatedDate()
                    );
                    double days = duration.toDays();
                    // Handle cases completed same day
                    if (days == 0 && duration.toHours() > 0) {
                        days = 0.5; // Half day for same-day completion
                    }
                    totalDays += days;
                    count++;
                }
            }

            return count > 0 ? Math.round((totalDays / count) * 10.0) / 10.0 : 0.0;
        } catch (Exception e) {
            log.error("Error calculating average processing days", e);
            return 0.0;
        }
    }

    public DashboardActivityDto getDashboardActivity(int limit, boolean includeUrgent) {
        log.debug("Fetching dashboard activity with limit: {}, includeUrgent: {}", limit, includeUrgent);

        // Fetch recent cases using Pageable for database-agnostic pagination
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"));
        List<Case> recentCases = caseRepository.findRecentCases(pageRequest);

        List<CaseActivityItem> recentActivityItems = recentCases.stream()
                .map(this::mapToCaseActivityItem)
                .collect(Collectors.toList());

        List<CaseActivityItem> urgentActivityItems = new ArrayList<>();
        if (includeUrgent) {
            // Fetch urgent cases (deadline within 2 days)
            Instant urgentThreshold = Instant.now().plus(2, ChronoUnit.DAYS);
            PageRequest urgentPageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "slaDeadline"));

            List<Case> urgentCases = caseRepository.findUrgentCases(
                    urgentThreshold,
                    EXCLUDED_STATUSES,
                    urgentPageRequest
            );

            urgentActivityItems = urgentCases.stream()
                    .map(this::mapToCaseActivityItem)
                    .collect(Collectors.toList());
        }

        return DashboardActivityDto.builder()
                .recentCases(recentActivityItems)
                .urgentCases(urgentActivityItems)
                .build();
    }

    private CaseActivityItem mapToCaseActivityItem(Case caseEntity) {
        Instant now = Instant.now();
        Duration duration = Duration.between(now, caseEntity.getSlaDeadline());
        long daysUntilDeadline = duration.toDays();

        // More accurate deadline calculation
        if (duration.isNegative()) {
            daysUntilDeadline = -Math.abs(duration.toDays());
        }

        DeadlineStatus deadlineStatus;
        if (daysUntilDeadline < 0) {
            deadlineStatus = DeadlineStatus.OVERDUE;
        } else if (daysUntilDeadline <= 2) {
            deadlineStatus = DeadlineStatus.URGENT;
        } else {
            deadlineStatus = DeadlineStatus.NORMAL;
        }

        return CaseActivityItem.builder()
                .caseId(caseEntity.getCaseId())
                .entityName(caseEntity.getEntityData().getEntityName())
                .entityType(caseEntity.getEntityData().getEntityType())
                .status(caseEntity.getStatus())
                .riskLevel(caseEntity.getRiskLevel())
                .createdDate(caseEntity.getCreatedDate())
                .slaDeadline(caseEntity.getSlaDeadline())
                .daysUntilDeadline((int) daysUntilDeadline)
                .deadlineStatus(deadlineStatus)
                .build();
    }

    private Instant getPeriodStart(String timeFilter, Instant now) {
        switch (timeFilter.toUpperCase()) {
            case "WEEK":
                return now.minus(7, ChronoUnit.DAYS);
            case "MONTH":
                return now.minus(30, ChronoUnit.DAYS);
            case "QUARTER":
                return now.minus(90, ChronoUnit.DAYS);
            default:
                return now.minus(7, ChronoUnit.DAYS);
        }
    }
}