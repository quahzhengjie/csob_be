package com.bkb.scanner.repository;

import com.bkb.scanner.entity.KycConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycConfigurationRepository extends JpaRepository<KycConfiguration, Long> {

    // ** THE FIX IS HERE: Add this method **
    // This allows the migration service to find the old JSON configuration
    // by its unique key, "DOCUMENT_REQUIREMENTS_TEMPLATE".
    Optional<KycConfiguration> findByConfigKey(String configKey);
}