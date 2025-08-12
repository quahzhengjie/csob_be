package com.bkb.scanner.controller;

import com.bkb.scanner.dto.DocumentRequirementsDto;
import com.bkb.scanner.dto.UpdateTemplateRequestDto;
import com.bkb.scanner.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/document-requirements")
    public ResponseEntity<DocumentRequirementsDto> getDocumentRequirements() {
        return ResponseEntity.ok(templateService.getDocumentRequirements());
    }

    @PutMapping("/document-requirements")
    @PreAuthorize("hasAuthority('admin:manage-templates')")
    public ResponseEntity<DocumentRequirementsDto> updateDocumentRequirements(
            @Valid @RequestBody UpdateTemplateRequestDto request) {
        return ResponseEntity.ok(templateService.updateDocumentRequirements(request));
    }
}