package com.bkb.scanner.service;

import com.bkb.scanner.entity.KycConfiguration;
import com.bkb.scanner.repository.KycConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KycConfigurationService {

    @Autowired
    private KycConfigurationRepository kycConfigurationRepository;

    @Transactional(readOnly = true)
    public String getDocumentRequirementsTemplate() {
        // Find the configuration by its specific key from your seed script
        KycConfiguration config = kycConfigurationRepository
                .findById("DOCUMENT_REQUIREMENTS_TEMPLATE")
                .orElseThrow(() -> new RuntimeException("Configuration not found: DOCUMENT_REQUIREMENTS_TEMPLATE"));

        return config.getConfigValue();
    }
}