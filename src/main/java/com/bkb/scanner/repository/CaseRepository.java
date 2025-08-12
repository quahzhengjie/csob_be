package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, String> {
    // Existing method - KEEP THIS
    long countByCreatedDateBetween(Instant startDate, Instant endDate);

    // Dashboard Statistics Queries - Using Spring Data JPA method names (database agnostic)
    Long countByStatus(String status);

    Long countByRiskLevel(String riskLevel);

    Long countByCreatedDateAfter(Instant date);

    Long countByStatusIn(List<String> statuses);

    // Using JPQL (database agnostic) - Hibernate will translate
    @Query("SELECT COUNT(c) FROM Case c WHERE c.slaDeadline < :now AND c.status NOT IN :excludedStatuses")
    Long countOverdueCases(@Param("now") Instant now, @Param("excludedStatuses") List<String> excludedStatuses);

    // Entity Type Distribution - Using JPQL projection
    @Query("SELECT c.entityData.entityType as entityType, COUNT(c) as count " +
            "FROM Case c " +
            "GROUP BY c.entityData.entityType " +
            "ORDER BY COUNT(c) DESC")
    List<EntityTypeCount> getEntityTypeDistribution();

    // Interface projection for type safety
    interface EntityTypeCount {
        String getEntityType();
        Long getCount();
    }

    // Recent Cases for Dashboard - FIXED: Remove the problematic method
    // Instead use this with Pageable
    @Query("SELECT c FROM Case c ORDER BY c.createdDate DESC")
    List<Case> findRecentCases(Pageable pageable);

    // Urgent Cases - JPQL with parameters
    @Query("SELECT c FROM Case c " +
            "WHERE c.slaDeadline < :urgentThreshold " +
            "AND c.status NOT IN :excludedStatuses " +
            "ORDER BY c.slaDeadline ASC")
    List<Case> findUrgentCases(@Param("urgentThreshold") Instant urgentThreshold,
                               @Param("excludedStatuses") List<String> excludedStatuses,
                               Pageable pageable);

    // Search with filters - Database agnostic JPQL
    @Query("SELECT DISTINCT c FROM Case c " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "       LOWER(c.entityData.entityName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "       LOWER(c.caseId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:hasRiskFilter = false OR c.riskLevel IN :riskLevels) " +
            "AND (:hasStatusFilter = false OR c.status IN :statuses)")
    Page<Case> findCasesWithFilters(@Param("search") String search,
                                    @Param("hasRiskFilter") boolean hasRiskFilter,
                                    @Param("riskLevels") List<String> riskLevels,
                                    @Param("hasStatusFilter") boolean hasStatusFilter,
                                    @Param("statuses") List<String> statuses,
                                    Pageable pageable);

    // For average processing time - fetch completed cases
    @Query("SELECT c FROM Case c LEFT JOIN FETCH c.activityLogs " +
            "WHERE c.status IN :completedStatuses")
    List<Case> findCompletedCasesWithActivityLogs(@Param("completedStatuses") List<String> completedStatuses);

    // Count cases by multiple criteria
    @Query("SELECT COUNT(c) FROM Case c " +
            "WHERE c.createdDate > :startDate " +
            "AND (:hasStatusFilter = false OR c.status IN :statuses)")
    Long countCasesByPeriodAndStatus(@Param("startDate") Instant startDate,
                                     @Param("hasStatusFilter") boolean hasStatusFilter,
                                     @Param("statuses") List<String> statuses);

    // Spring Data JPA derived query methods (completely database agnostic)
    List<Case> findByCreatedDateAfterOrderByCreatedDateDesc(Instant date);

    List<Case> findByStatusAndSlaDeadlineBefore(String status, Instant deadline);

    Long countByStatusAndCreatedDateAfter(String status, Instant date);
}