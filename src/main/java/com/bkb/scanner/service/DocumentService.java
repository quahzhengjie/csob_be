package com.bkb.scanner.service;

import com.bkb.scanner.dto.DocumentDto;
import com.bkb.scanner.entity.Case;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.entity.Party;
import com.bkb.scanner.mapper.DocumentMapper;
import com.bkb.scanner.repository.CaseRepository;
import com.bkb.scanner.repository.DocumentRepository;
import com.bkb.scanner.repository.PartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private DocumentMapper documentMapper;

    /**
     * Upload a document for a case with metadata
     */
    public DocumentDto uploadDocumentForCase(String caseId, String documentType, MultipartFile file,
                                             String expiryDate, String comments) throws IOException {
        // Find the case
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));

        // Get the next version number
        int nextVersion = getNextVersionNumber("CASE", caseId, documentType);
        System.out.println("📄 Uploading document for case: " + documentType + " (version " + nextVersion + ")");

        // ✅ Clear current flags for THIS document type only for THIS case
        List<Document> existingDocs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                "CASE", caseId, documentType
        );
        existingDocs.forEach(doc -> doc.setIsCurrentForCase(false));
        documentRepository.saveAll(existingDocs);

        // Create the document
        Document document = new Document();
        document.setName(documentType);
        document.setDocumentType(documentType);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setMimeType(file.getContentType());
        document.setSizeInBytes(file.getSize());
        document.setContent(file.getBytes());
        document.setOwnerType("CASE");
        document.setOwnerId(caseId);
        document.setOwnerCase(ownerCase);
        document.setStatus("Submitted");
        document.setVersion(nextVersion);
        document.setUploadedBy(getCurrentUsername());
        document.setIsCurrentForCase(true); // ✅ New version is current by default

        // ✅ NEW: Set expiry date and comments
        if (expiryDate != null && !expiryDate.trim().isEmpty()) {
            try {
                // Parse ISO date string to Instant
                document.setExpiryDate(Instant.parse(expiryDate + "T00:00:00Z"));
            } catch (Exception e) {
                System.err.println("Failed to parse expiry date: " + expiryDate);
            }
        }
        if (comments != null && !comments.trim().isEmpty()) {
            document.setComments(comments);
        }

        Document saved = documentRepository.save(document);
        System.out.println("✅ Document saved with version: " + saved.getVersion() + " as current");
        System.out.println("  - Expiry date: " + saved.getExpiryDate());
        System.out.println("  - Comments: " + saved.getComments());
        return documentMapper.toDto(saved);
    }

    /**
     * Upload a document for a party with metadata
     */
    public DocumentDto uploadDocumentForParty(String partyId, String documentType, MultipartFile file,
                                              String expiryDate, String comments) throws IOException {
        // Find the party
        Party ownerParty = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Get the next version number
        int nextVersion = getNextVersionNumber("PARTY", partyId, documentType);
        System.out.println("📄 Uploading document for party: " + documentType + " (version " + nextVersion + ")");

        // ✅ For party documents, clear current flags for this document type for this party
        List<Document> existingDocs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                "PARTY", partyId, documentType
        );
        existingDocs.forEach(doc -> doc.setIsCurrentForCase(false));
        documentRepository.saveAll(existingDocs);

        // Create the document
        Document document = new Document();
        document.setName(documentType);
        document.setDocumentType(documentType);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setMimeType(file.getContentType());
        document.setSizeInBytes(file.getSize());
        document.setContent(file.getBytes());
        document.setOwnerType("PARTY");
        document.setOwnerId(partyId);
        document.setOwnerParty(ownerParty);
        document.setStatus("Submitted");
        document.setVersion(nextVersion);
        document.setUploadedBy(getCurrentUsername());
        document.setIsCurrentForCase(true); // ✅ New version is current by default

        // ✅ NEW: Set expiry date and comments
        if (expiryDate != null && !expiryDate.trim().isEmpty()) {
            try {
                // Parse ISO date string to Instant
                document.setExpiryDate(Instant.parse(expiryDate + "T00:00:00Z"));
            } catch (Exception e) {
                System.err.println("Failed to parse expiry date: " + expiryDate);
            }
        }
        if (comments != null && !comments.trim().isEmpty()) {
            document.setComments(comments);
        }

        Document saved = documentRepository.save(document);
        System.out.println("✅ Document saved with version: " + saved.getVersion() + " as current");
        System.out.println("  - Expiry date: " + saved.getExpiryDate());
        System.out.println("  - Comments: " + saved.getComments());
        return documentMapper.toDto(saved);
    }

    /**
     * Calculate the next version number for a document
     */
    private int getNextVersionNumber(String ownerType, String ownerId, String documentType) {
        // Find all existing versions of this document type for this owner
        List<Document> existingDocs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                ownerType, ownerId, documentType
        );

        if (existingDocs.isEmpty()) {
            return 1; // First version
        }

        // Find the highest version number
        int maxVersion = existingDocs.stream()
                .mapToInt(Document::getVersion)
                .max()
                .orElse(0);

        System.out.println("🔢 Found " + existingDocs.size() + " existing versions, max version: " + maxVersion);
        return maxVersion + 1;
    }

    /**
     * Get all documents for a case (including party documents)
     */
    public List<DocumentDto> getAllDocumentsForCase(String caseId) {
        List<Document> documents = documentRepository.findAllDocumentsForCase(caseId);
        return documents.stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get documents for a party
     */
    public List<DocumentDto> getDocumentsForParty(String partyId) {
        List<Document> documents = documentRepository.findByPartyId(partyId);
        return documents.stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update document status
     */
    public DocumentDto updateDocumentStatus(Long documentId, String status, String rejectionReason) {
        System.out.println("🔄 UPDATE DOCUMENT STATUS CALLED:");
        System.out.println("- documentId: " + documentId);
        System.out.println("- status: " + status);
        System.out.println("- rejectionReason: " + rejectionReason);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        System.out.println("- found document: " + document.getName() + " (current status: " + document.getStatus() + ")");

        document.setStatus(status);
        if ("Rejected".equals(status) && rejectionReason != null) {
            document.setRejectionReason(rejectionReason);
        }

        if ("Verified".equals(status)) {
            document.setVerifiedBy(getCurrentUsername());
            document.setVerifiedDate(Instant.now());
        }

        Document updated = documentRepository.save(document);
        System.out.println("✅ Document status updated successfully to: " + updated.getStatus());
        return documentMapper.toDto(updated);
    }

    /**
     * Get document with content for download
     */
    public Optional<Document> getDocumentWithContent(Long documentId) {
        return documentRepository.findById(documentId);
    }

    /**
     * Get document status summary
     */
    public DocumentStatusSummary getDocumentStatusSummary(String ownerType, String ownerId) {
        List<Document> documents = documentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);

        long total = documents.size();
        long verified = documents.stream().filter(d -> "Verified".equals(d.getStatus())).count();
        long submitted = documents.stream().filter(d -> "Submitted".equals(d.getStatus())).count();
        long rejected = documents.stream().filter(d -> "Rejected".equals(d.getStatus())).count();
        long expired = documents.stream().filter(d -> "Expired".equals(d.getStatus())).count();

        return new DocumentStatusSummary(total, verified, submitted, rejected, expired);
    }

    /**
     * ✅ Update which document version is current for a case
     */
    @Transactional
    public void updateCurrentDocumentVersion(String caseId, String documentType, Long newCurrentVersionId) {
        System.out.println("🔄 Updating current document version:");
        System.out.println("- caseId: " + caseId);
        System.out.println("- documentType: " + documentType);
        System.out.println("- newCurrentVersionId: " + newCurrentVersionId);

        // Step 1: Get the document that will become current
        Document newCurrentDoc = documentRepository.findById(newCurrentVersionId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + newCurrentVersionId));

        // Step 2: Clear all current flags for this document type
        // For case documents
        if ("CASE".equals(newCurrentDoc.getOwnerType())) {
            List<Document> docs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                    "CASE", caseId, documentType
            );
            docs.forEach(doc -> doc.setIsCurrentForCase(false));
            documentRepository.saveAll(docs);
        }
        // For party documents
        else if ("PARTY".equals(newCurrentDoc.getOwnerType())) {
            List<Document> docs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                    "PARTY", newCurrentDoc.getOwnerId(), documentType
            );
            docs.forEach(doc -> doc.setIsCurrentForCase(false));
            documentRepository.saveAll(docs);
        }

        // Step 3: Set the new version as current
        newCurrentDoc.setIsCurrentForCase(true);
        documentRepository.save(newCurrentDoc);

        System.out.println("✅ Document version " + newCurrentDoc.getVersion() + " is now current for case " + caseId);
    }

    /**
     * ✅ Check if a document belongs to a case
     */
    public boolean documentBelongsToCase(Document document, String caseId) {
        if ("CASE".equals(document.getOwnerType()) && caseId.equals(document.getOwnerId())) {
            return true;
        } else if ("PARTY".equals(document.getOwnerType())) {
            return documentRepository.isPartyRelatedToCase(document.getOwnerId(), caseId);
        }
        return false;
    }

    private String getCurrentUsername() {
        // In a real application, this would get the username from SecurityContext
        return "SYSTEM";
    }

    public static class DocumentStatusSummary {
        public final long total;
        public final long verified;
        public final long submitted;
        public final long rejected;
        public final long expired;

        public DocumentStatusSummary(long total, long verified, long submitted, long rejected, long expired) {
            this.total = total;
            this.verified = verified;
            this.submitted = submitted;
            this.rejected = rejected;
            this.expired = expired;
        }
    }
}