package com.bkb.scanner.controller;

import com.bkb.scanner.dto.ScanTriggerRequest;
import com.bkb.scanner.dto.ScanTriggerResponse;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.service.ScanTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("scans")
public class ScanController {

    @Autowired
    private ScanTriggerService scanTriggerService;

    @PostMapping("/trigger")
    @PreAuthorize("hasAuthority('document:upload')")
    public ResponseEntity<ScanTriggerResponse> triggerScan(@RequestBody ScanTriggerRequest request) {
        System.out.println("Received scan request: " + request);
        System.out.println("Profile Name: " + request.getProfileName());
        System.out.println("Owner Type: " + request.getOwnerType());
        System.out.println("Owner ID: " + request.getOwnerId());
        System.out.println("Document Type: " + request.getDocumentType());
        System.out.println("Format: " + request.getFormat());

        try {
            Document scannedDoc = scanTriggerService.triggerNaps2Scan(
                    request.getProfileName(),
                    request.getOwnerType(),
                    request.getOwnerId(),
                    request.getDocumentType(),
                    request.getFormat() // Pass format to service
            );

            ScanTriggerResponse response = new ScanTriggerResponse();
            response.setDocumentId(scannedDoc.getId().toString());
            response.setStatus("SUCCESS");
            response.setMessage("Document scanned successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Scan failed: " + e.getMessage());
            e.printStackTrace();

            ScanTriggerResponse response = new ScanTriggerResponse();
            response.setStatus("ERROR");
            response.setMessage("Scan failed: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}