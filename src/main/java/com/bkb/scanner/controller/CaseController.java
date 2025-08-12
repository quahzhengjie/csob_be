//package com.bkb.scanner.controller;
//
//import com.bkb.scanner.dto.*;
//import com.bkb.scanner.entity.Document;
//import com.bkb.scanner.service.CaseService;
//import com.bkb.scanner.service.DocumentService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/cases")
//public class CaseController {
//
//    @Autowired
//    private CaseService caseService;
//
//    @Autowired
//    private DocumentService documentService;
//
//    @GetMapping
//    @PreAuthorize("hasAuthority('case:read')")
//    public ResponseEntity<List<CaseDto>> getAllCases() {
//        return ResponseEntity.ok(caseService.getAllCases());
//    }
//
//    @PostMapping
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CaseDto> createCase(@Valid @RequestBody CaseCreationRequest request) {
//        return new ResponseEntity<>(caseService.createCase(request), HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{caseId}")
//    @PreAuthorize("hasAuthority('case:read')")
//    public ResponseEntity<CaseDto> getCaseById(@PathVariable String caseId) {
//        return caseService.getCaseById(caseId)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PatchMapping("/{caseId}/assign")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CaseDto> assignCase(@PathVariable String caseId, @Valid @RequestBody AssignCaseRequest request) {
//        return ResponseEntity.ok(caseService.assignCase(caseId, request.getUserId()));
//    }
//
//    @PatchMapping("/{caseId}/status")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CaseDto> updateCaseStatus(@PathVariable String caseId, @Valid @RequestBody CaseUpdateRequest request) {
//        return ResponseEntity.ok(caseService.updateCaseStatus(caseId, request.getStatus(), request.getRiskLevel()));
//    }
//
//    @PostMapping("/{caseId}/reports")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CallReportDto> addCallReport(@PathVariable String caseId, @Valid @RequestBody CallReportDto reportDto) {
//        return new ResponseEntity<>(caseService.addCallReport(caseId, reportDto), HttpStatus.CREATED);
//    }
//
//    @PostMapping("/{caseId}/activities")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<ActivityLogDto> addActivityLog(@PathVariable String caseId, @Valid @RequestBody ActivityLogDto activityDto) {
//        return new ResponseEntity<>(caseService.addActivityLog(caseId, activityDto), HttpStatus.CREATED);
//    }
//
//    @PatchMapping("/{caseId}/entity")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CaseDto> updateEntityData(
//            @PathVariable String caseId,
//            @Valid @RequestBody EntityDataDto entityData) {
//        return ResponseEntity.ok(caseService.updateEntityData(caseId, entityData));
//    }
//
//    @PostMapping("/{caseId}/parties")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<RelatedPartyDto> addRelatedPartyToCase(
//            @PathVariable String caseId,
//            @Valid @RequestBody RelatedPartyDto partyDto
//    ) {
//        RelatedPartyDto newParty = caseService.addRelatedParty(caseId, partyDto);
//        return new ResponseEntity<>(newParty, HttpStatus.CREATED);
//    }
//
//    @DeleteMapping("/{caseId}/parties/{partyId}")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<Void> removeRelatedPartyFromCase(
//            @PathVariable String caseId,
//            @PathVariable String partyId,
//            @RequestParam String relationshipType
//    ) {
//        caseService.removeRelatedParty(caseId, partyId, relationshipType);
//        return ResponseEntity.noContent().build();
//    }
//
//    /**
//     * Update which document version is linked to a case
//     * ✅ COMPLETE IMPLEMENTATION WITH PERSISTENCE
//     */
//    @PatchMapping("/{caseId}/documents/{documentId}/link")
//    @PreAuthorize("hasAuthority('case:update')")
//    @Transactional
//    public ResponseEntity<Map<String, Object>> updateDocumentLink(
//            @PathVariable String caseId,
//            @PathVariable String documentId,
//            @RequestBody Map<String, String> request) {
//
//        String versionId = request.get("versionId");
//
//        System.out.println("🔄 UPDATE DOCUMENT LINK:");
//        System.out.println("- caseId: " + caseId);
//        System.out.println("- documentId: " + documentId);
//        System.out.println("- versionId: " + versionId);
//
//        // Validate inputs
//        if (versionId == null || versionId.trim().isEmpty()) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", "Version ID is required");
//            return ResponseEntity.badRequest().body(error);
//        }
//
//        try {
//            // Parse the version ID (which is actually the document ID in the database)
//            Long docId = Long.parseLong(versionId);
//
//            // Validate the document exists
//            Document document = documentService.getDocumentWithContent(docId)
//                    .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));
//
//            // Business rule: Only verified documents can be made current
//            if (!"Verified".equals(document.getStatus())) {
//                Map<String, Object> error = new HashMap<>();
//                error.put("error", "Only Verified documents can be made current");
//                error.put("currentStatus", document.getStatus());
//                error.put("documentName", document.getName());
//                return ResponseEntity.badRequest().body(error);
//            }
//
//            // Validate the document belongs to this case
//            if (!documentService.documentBelongsToCase(document, caseId)) {
//                Map<String, Object> error = new HashMap<>();
//                error.put("error", "Document does not belong to this case");
//                error.put("documentOwner", document.getOwnerType() + ":" + document.getOwnerId());
//                return ResponseEntity.badRequest().body(error);
//            }
//
//            // ✅ ACTUAL IMPLEMENTATION - Update the current version
//            documentService.updateCurrentDocumentVersion(caseId, document.getDocumentType(), docId);
//
//            // Add audit log
//            ActivityLogDto activityLog = new ActivityLogDto();
//            activityLog.setType("document_made_current");
//            activityLog.setDetails(String.format("Made %s v%d the current version",
//                    document.getDocumentType(), document.getVersion()));
//            caseService.addActivityLog(caseId, activityLog);
//
//            // Return success response
//            Map<String, Object> response = new HashMap<>();
//            response.put("linkId", "LNK-" + caseId + "-" + versionId);
//            response.put("caseId", caseId);
//            response.put("documentId", documentId);
//            response.put("versionId", versionId);
//            response.put("status", document.getStatus());
//            response.put("documentName", document.getName());
//            response.put("version", document.getVersion());
//            response.put("message", "Document version updated successfully");
//            response.put("isCurrentForCase", true);
//
//            System.out.println("✅ Document link updated successfully");
//            System.out.println("- Document: " + document.getName() + " v" + document.getVersion());
//            System.out.println("- Status: " + document.getStatus());
//
//            return ResponseEntity.ok(response);
//
//        } catch (NumberFormatException e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", "Invalid version ID format. Expected a number, got: " + versionId);
//            return ResponseEntity.badRequest().body(error);
//        } catch (RuntimeException e) {
//            System.err.println("❌ Runtime error: " + e.getMessage());
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//        } catch (Exception e) {
//            System.err.println("❌ Unexpected error updating document link: " + e.getMessage());
//            e.printStackTrace();
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", "An unexpected error occurred");
//            error.put("details", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
//        }
//    }
//
//    @PatchMapping("/{caseId}/reports/{reportId}")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<CallReportDto> updateCallReport(
//            @PathVariable String caseId,
//            @PathVariable Long reportId,
//            @Valid @RequestBody CallReportDto reportDto) {
//        return ResponseEntity.ok(caseService.updateCallReport(caseId, reportId, reportDto));
//    }
//
//    @DeleteMapping("/{caseId}/reports/{reportId}")
//    @PreAuthorize("hasAuthority('case:update')")
//    public ResponseEntity<Void> deleteCallReport(
//            @PathVariable String caseId,
//            @PathVariable Long reportId,
//            @RequestParam(required = false, defaultValue = "Deleted by user") String reason) {
//        caseService.deleteCallReport(caseId, reportId, reason);
//        return ResponseEntity.noContent().build();
//    }
//
//}

package com.bkb.scanner.controller;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.service.CaseService;
import com.bkb.scanner.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/cases")
public class CaseController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private DocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<CaseDto>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllCases());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CaseDto> createCase(@Valid @RequestBody CaseCreationRequest request) {
        return new ResponseEntity<>(caseService.createCase(request), HttpStatus.CREATED);
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<CaseDto> getCaseById(@PathVariable String caseId) {
        return caseService.getCaseById(caseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{caseId}/assign")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CaseDto> assignCase(@PathVariable String caseId, @Valid @RequestBody AssignCaseRequest request) {
        return ResponseEntity.ok(caseService.assignCase(caseId, request.getUserId()));
    }

    @PatchMapping("/{caseId}/status")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CaseDto> updateCaseStatus(@PathVariable String caseId, @Valid @RequestBody CaseUpdateRequest request) {
        return ResponseEntity.ok(caseService.updateCaseStatus(caseId, request.getStatus(), request.getRiskLevel()));
    }

    @PostMapping("/{caseId}/reports")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CallReportDto> addCallReport(@PathVariable String caseId, @Valid @RequestBody CallReportDto reportDto) {
        return new ResponseEntity<>(caseService.addCallReport(caseId, reportDto), HttpStatus.CREATED);
    }

    @PostMapping("/{caseId}/activities")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<ActivityLogDto> addActivityLog(@PathVariable String caseId, @Valid @RequestBody ActivityLogDto activityDto) {
        return new ResponseEntity<>(caseService.addActivityLog(caseId, activityDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{caseId}/entity")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CaseDto> updateEntityData(
            @PathVariable String caseId,
            @Valid @RequestBody EntityDataDto entityData) {
        return ResponseEntity.ok(caseService.updateEntityData(caseId, entityData));
    }

    @PostMapping("/{caseId}/parties")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<RelatedPartyDto> addRelatedPartyToCase(
            @PathVariable String caseId,
            @Valid @RequestBody RelatedPartyDto partyDto
    ) {
        RelatedPartyDto newParty = caseService.addRelatedParty(caseId, partyDto);
        return new ResponseEntity<>(newParty, HttpStatus.CREATED);
    }

    @DeleteMapping("/{caseId}/parties/{partyId}")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<Void> removeRelatedPartyFromCase(
            @PathVariable String caseId,
            @PathVariable String partyId,
            @RequestParam String relationshipType
    ) {
        caseService.removeRelatedParty(caseId, partyId, relationshipType);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update which document version is linked to a case
     * ✅ COMPLETE IMPLEMENTATION WITH PERSISTENCE
     */
    @PatchMapping("/{caseId}/documents/{documentId}/link")
    @PreAuthorize("hasAuthority('case:update')")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateDocumentLink(
            @PathVariable String caseId,
            @PathVariable String documentId,
            @RequestBody Map<String, String> request) {

        String versionId = request.get("versionId");

        System.out.println("🔄 UPDATE DOCUMENT LINK:");
        System.out.println("- caseId: " + caseId);
        System.out.println("- documentId: " + documentId);
        System.out.println("- versionId: " + versionId);

        // Validate inputs
        if (versionId == null || versionId.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Version ID is required");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Parse the version ID (which is actually the document ID in the database)
            Long docId = Long.parseLong(versionId);

            // Validate the document exists
            Document document = documentService.getDocumentWithContent(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found with ID: " + docId));

            // Business rule: Only verified documents can be made current
            if (!"Verified".equals(document.getStatus())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Only Verified documents can be made current");
                error.put("currentStatus", document.getStatus());
                error.put("documentName", document.getName());
                return ResponseEntity.badRequest().body(error);
            }

            // Validate the document belongs to this case
            if (!documentService.documentBelongsToCase(document, caseId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Document does not belong to this case");
                error.put("documentOwner", document.getOwnerType() + ":" + document.getOwnerId());
                return ResponseEntity.badRequest().body(error);
            }

            // ✅ ACTUAL IMPLEMENTATION - Update the current version
            documentService.updateCurrentDocumentVersion(caseId, document.getDocumentType(), docId);

            // Add audit log
            ActivityLogDto activityLog = new ActivityLogDto();
            activityLog.setType("document_made_current");
            activityLog.setDetails(String.format("Made %s v%d the current version",
                    document.getDocumentType(), document.getVersion()));
            caseService.addActivityLog(caseId, activityLog);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("linkId", "LNK-" + caseId + "-" + versionId);
            response.put("caseId", caseId);
            response.put("documentId", documentId);
            response.put("versionId", versionId);
            response.put("status", document.getStatus());
            response.put("documentName", document.getName());
            response.put("version", document.getVersion());
            response.put("message", "Document version updated successfully");
            response.put("isCurrentForCase", true);

            System.out.println("✅ Document link updated successfully");
            System.out.println("- Document: " + document.getName() + " v" + document.getVersion());
            System.out.println("- Status: " + document.getStatus());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid version ID format. Expected a number, got: " + versionId);
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error updating document link: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PatchMapping("/{caseId}/reports/{reportId}")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<CallReportDto> updateCallReport(
            @PathVariable String caseId,
            @PathVariable Long reportId,
            @Valid @RequestBody CallReportDto reportDto) {
        return ResponseEntity.ok(caseService.updateCallReport(caseId, reportId, reportDto));
    }

    @DeleteMapping("/{caseId}/reports/{reportId}")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<Void> deleteCallReport(
            @PathVariable String caseId,
            @PathVariable Long reportId,
            @RequestParam(required = false, defaultValue = "Deleted by user") String reason) {
        caseService.deleteCallReport(caseId, reportId, reason);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // NEW ENDPOINT FOR OPTIMIZED CASE SEARCH
    // =====================================================================

    /**
     * Get cases with server-side filtering, sorting, and pagination
     *
     * @param page Current page number (1-based)
     * @param limit Number of items per page
     * @param search Search term for entity name or case ID
     * @param riskLevel Comma-separated risk levels (e.g., "High,Medium")
     * @param status Comma-separated statuses (e.g., "KYC Review,Pending Approval")
     * @param sortBy Field to sort by (entityName, caseId, riskLevel, status, createdDate)
     * @param sortOrder Sort direction (asc or desc)
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<CasesPageDto> searchCases(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("Searching cases - page: {}, limit: {}, search: {}, riskLevel: {}, status: {}",
                page, limit, search, riskLevel, status);

        // Validate pagination parameters
        if (page < 1) page = 1;
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;

        CasesPageDto result = caseService.getCasesWithFilters(
                page, limit, search, riskLevel, status, sortBy, sortOrder
        );

        return ResponseEntity.ok(result);
    }
}