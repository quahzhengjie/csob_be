package com.bkb.scanner.controller;

import com.bkb.scanner.dto.ScannerProfileDto;
import com.bkb.scanner.service.ScannerProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/scanner-profiles")
public class ScannerProfileController {

    @Autowired
    private ScannerProfileService profileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScannerProfileDto>> getAllProfiles() {
        return ResponseEntity.ok(profileService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:manage-templates')")
    public ResponseEntity<ScannerProfileDto> createProfile(@Valid @RequestBody ScannerProfileDto dto) {
        return new ResponseEntity<>(profileService.create(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:manage-templates')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}