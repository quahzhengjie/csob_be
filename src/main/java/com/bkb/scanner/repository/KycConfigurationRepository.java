package com.bkb.scanner.repository;

import com.bkb.scanner.entity.KycConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycConfigurationRepository extends JpaRepository<KycConfiguration, String> {
}