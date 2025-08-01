package com.bkb.scanner.repository;

import com.bkb.scanner.entity.CallReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallReportRepository extends JpaRepository<CallReport, Long> {}