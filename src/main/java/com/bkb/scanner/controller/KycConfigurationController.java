package com.bkb.scanner.controller;

import com.bkb.scanner.service.KycConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configurations")
public class KycConfigurationController {

    @Autowired
    private KycConfigurationService kycConfigurationService;

    @GetMapping("/document-requirements")
    public ResponseEntity<String> getDocumentRequirements() {
        String templateJson = kycConfigurationService.getDocumentRequirementsTemplate();

        // Return the JSON string with the correct content type
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(templateJson);
    }
}