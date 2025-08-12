package com.bkb.scanner.repository;

import com.bkb.scanner.entity.ScannerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScannerProfileRepository extends JpaRepository<ScannerProfile, Long> {}