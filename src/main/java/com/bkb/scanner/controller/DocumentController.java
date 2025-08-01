package com.bkb.scanner.controller;

import com.bkb.scanner.dto.DocumentDto;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.service.DocumentService;
import com.bkb.scanner.service.DocumentService.DocumentStatusSummary;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Upload document for a case with metadata
     */
    @PostMapping("/upload/case/{caseId}")
    @PreAuthorize("hasAuthority('document:upload')")
    public ResponseEntity<DocumentDto> uploadDocumentForCase(
            @PathVariable String caseId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "comments", required = false) String comments) throws IOException {

        System.out.println("📤 Upload request received for case:");
        System.out.println("  - Case ID: " + caseId);
        System.out.println("  - Document type: " + documentType);
        System.out.println("  - File: " + file.getOriginalFilename());
        System.out.println("  - Expiry date: " + expiryDate);
        System.out.println("  - Comments: " + comments);

        DocumentDto dto = documentService.uploadDocumentForCase(caseId, documentType, file, expiryDate, comments);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Upload document for a party with metadata
     */
    @PostMapping("/upload/party/{partyId}")
    @PreAuthorize("hasAuthority('document:upload')")
    public ResponseEntity<DocumentDto> uploadDocumentForParty(
            @PathVariable String partyId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "comments", required = false) String comments) throws IOException {

        System.out.println("📤 Upload request received for party:");
        System.out.println("  - Party ID: " + partyId);
        System.out.println("  - Document type: " + documentType);
        System.out.println("  - File: " + file.getOriginalFilename());
        System.out.println("  - Expiry date: " + expiryDate);
        System.out.println("  - Comments: " + comments);

        DocumentDto dto = documentService.uploadDocumentForParty(partyId, documentType, file, expiryDate, comments);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Legacy endpoint for backward compatibility
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('document:upload')")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("caseId") String caseId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "comments", required = false) String comments) throws IOException {

        // Default to case upload for backward compatibility
        return uploadDocumentForCase(caseId, documentType, file, expiryDate, comments);
    }

    /**
     * Get all documents for a case (including party documents)
     */
    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<DocumentDto>> getDocumentsForCase(@PathVariable String caseId) {
        return ResponseEntity.ok(documentService.getAllDocumentsForCase(caseId));
    }

    /**
     * Get documents for a party
     */
    @GetMapping("/party/{partyId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<DocumentDto>> getDocumentsForParty(@PathVariable String partyId) {
        return ResponseEntity.ok(documentService.getDocumentsForParty(partyId));
    }

    /**
     * Update document status
     */
    @PatchMapping("/{documentId}/status")
    @PreAuthorize("hasAuthority('document:verify')")
    public ResponseEntity<DocumentDto> updateDocumentStatus(
            @PathVariable Long documentId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {

        DocumentDto dto = documentService.updateDocumentStatus(documentId, status, rejectionReason);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get document status summary
     */
    @GetMapping("/status-summary/{ownerType}/{ownerId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<DocumentStatusSummary> getDocumentStatusSummary(
            @PathVariable String ownerType,
            @PathVariable String ownerId) {

        return ResponseEntity.ok(documentService.getDocumentStatusSummary(ownerType, ownerId));
    }

    /**
     * Download document
     */
    @GetMapping("/download/{documentId}")
    @PreAuthorize("hasAuthority('document:read')")
    public void downloadDocument(@PathVariable Long documentId, HttpServletResponse response) throws IOException {
        Document doc = documentService.getDocumentWithContent(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        response.setContentType(doc.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + doc.getOriginalFilename() + "\"");
        response.setContentLengthLong(doc.getSizeInBytes());
        response.getOutputStream().write(doc.getContent());
        response.getOutputStream().flush();
    }

    // REMOVED: makeDocumentCurrentForCase endpoint
    // REMOVED: makeDocumentCurrent endpoint
    // The "make current" functionality is now handled in CaseController
}