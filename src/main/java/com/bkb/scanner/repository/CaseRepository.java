package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public interface CaseRepository extends JpaRepository<Case, String> {
    // Add this method for counting cases in a date range
    long countByCreatedDateBetween(Instant startDate, Instant endDate);
}