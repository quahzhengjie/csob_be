package com.bkb.scanner.repository;

import com.bkb.scanner.entity.CallReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallReportRepository extends JpaRepository<CallReport, Long> {

    // Find only non-deleted reports for a case
    @Query("SELECT cr FROM CallReport cr WHERE cr.ownerCase.caseId = :caseId AND cr.isDeleted = false ORDER BY cr.callDate DESC")
    List<CallReport> findActiveByCaseId(@Param("caseId") String caseId);

    // Find by ID only if not deleted
    @Query("SELECT cr FROM CallReport cr WHERE cr.id = :id AND cr.isDeleted = false")
    Optional<CallReport> findActiveById(@Param("id") Long id);

    // Find all reports including deleted (for audit purposes)
    @Query("SELECT cr FROM CallReport cr WHERE cr.ownerCase.caseId = :caseId ORDER BY cr.callDate DESC")
    List<CallReport> findAllByCaseIdIncludingDeleted(@Param("caseId") String caseId);
}