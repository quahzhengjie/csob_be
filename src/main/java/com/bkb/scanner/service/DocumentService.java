package com.bkb.scanner.service;

import com.bkb.scanner.dto.DocumentDto;
import com.bkb.scanner.entity.Case;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.entity.Party;
import com.bkb.scanner.entity.User;
import com.bkb.scanner.exception.SelfVerificationException;
import com.bkb.scanner.mapper.DocumentMapper;
import com.bkb.scanner.repository.CaseRepository;
import com.bkb.scanner.repository.DocumentRepository;
import com.bkb.scanner.repository.PartyRepository;
import com.bkb.scanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private UserRepository userRepository;

    @Autowired
    private DocumentMapper documentMapper;

    /**
     * Upload a document for a case with metadata
     */
    public DocumentDto uploadDocumentForCase(String caseId, String documentType, MultipartFile file,
                                             String expiryDate, String comments, Boolean isAdHoc) throws IOException {
        // Find the case
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found with id: " + caseId));

        // Get the current user
        User currentUser = getCurrentUser();

        // Get the next version number
        int nextVersion = getNextVersionNumber("CASE", caseId, documentType);
        System.out.println("📄 Uploading document for case: " + documentType + " (version " + nextVersion + ")");

        // Clear current flags for THIS document type only for THIS case
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
        document.setUploadedByUser(currentUser);
        document.setIsCurrentForCase(false);
        document.setIsAdHoc(isAdHoc != null ? isAdHoc : false);

        if (expiryDate != null && !expiryDate.trim().isEmpty()) {
            try {
                document.setExpiryDate(Instant.parse(expiryDate + "T00:00:00Z"));
            } catch (Exception e) {
                System.err.println("Failed to parse expiry date: " + expiryDate);
            }
        }
        if (comments != null && !comments.trim().isEmpty()) {
            document.setComments(comments);
        }

        Document saved = documentRepository.save(document);
        return documentMapper.toDto(saved);
    }

    /**
     * Upload a document for a party with metadata
     */
    public DocumentDto uploadDocumentForParty(String partyId, String documentType, MultipartFile file,
                                              String expiryDate, String comments, Boolean isAdHoc) throws IOException {
        // Find the party
        Party ownerParty = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Get the current user
        User currentUser = getCurrentUser();

        // Get the next version number
        int nextVersion = getNextVersionNumber("PARTY", partyId, documentType);
        System.out.println("📄 Uploading document for party: " + documentType + " (version " + nextVersion + ")");

        // For party documents, clear current flags for this document type for this party
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
        document.setUploadedByUser(currentUser);
        document.setIsCurrentForCase(false);
        document.setIsAdHoc(isAdHoc != null ? isAdHoc : false);

        if (expiryDate != null && !expiryDate.trim().isEmpty()) {
            try {
                document.setExpiryDate(Instant.parse(expiryDate + "T00:00:00Z"));
            } catch (Exception e) {
                System.err.println("Failed to parse expiry date: " + expiryDate);
            }
        }
        if (comments != null && !comments.trim().isEmpty()) {
            document.setComments(comments);
        }

        Document saved = documentRepository.save(document);
        return documentMapper.toDto(saved);
    }

    /**
     * Calculate the next version number for a document
     */
    private int getNextVersionNumber(String ownerType, String ownerId, String documentType) {
        List<Document> existingDocs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                ownerType, ownerId, documentType
        );

        if (existingDocs.isEmpty()) {
            return 1;
        }

        int maxVersion = existingDocs.stream()
                .mapToInt(Document::getVersion)
                .max()
                .orElse(0);

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
    public List<DocumentDto> getAllDocumentsForParty(String partyId) {
        List<Document> documents = documentRepository.findByOwnerTypeAndOwnerId("PARTY", partyId);
        return documents.stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update document status with self-verification prevention
     */
    public DocumentDto updateDocumentStatus(Long documentId, String status, String rejectionReason) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User currentUser = getCurrentUser();

        // Prevent self-verification - check user ID
        if ("Verified".equals(status) &&
                document.getUploadedByUser() != null &&
                document.getUploadedByUser().getUserId().equals(currentUser.getUserId())) {
            throw new SelfVerificationException(documentId.toString(), currentUser.getUsername());
        }

        document.setStatus(status);
        if ("Rejected".equals(status) && rejectionReason != null) {
            document.setRejectionReason(rejectionReason);
        }

        if ("Verified".equals(status)) {
            document.setVerifiedByUser(currentUser);
            document.setVerifiedDate(Instant.now());
        }

        Document updated = documentRepository.save(document);
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
     * Update which document version is current for a case
     */
    @Transactional
    public void updateCurrentDocumentVersion(String caseId, String documentType, Long newCurrentVersionId) {
        Document newCurrentDoc = documentRepository.findById(newCurrentVersionId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + newCurrentVersionId));

        if ("CASE".equals(newCurrentDoc.getOwnerType())) {
            List<Document> docs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                    "CASE", caseId, documentType
            );
            docs.forEach(doc -> doc.setIsCurrentForCase(false));
            documentRepository.saveAll(docs);
        } else if ("PARTY".equals(newCurrentDoc.getOwnerType())) {
            List<Document> docs = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentType(
                    "PARTY", newCurrentDoc.getOwnerId(), documentType
            );
            docs.forEach(doc -> doc.setIsCurrentForCase(false));
            documentRepository.saveAll(docs);
        }

        newCurrentDoc.setIsCurrentForCase(true);
        documentRepository.save(newCurrentDoc);
    }

    /**
     * Check if a document belongs to a case
     */
    public boolean documentBelongsToCase(Document document, String caseId) {
        if ("CASE".equals(document.getOwnerType()) && caseId.equals(document.getOwnerId())) {
            return true;
        } else if ("PARTY".equals(document.getOwnerType())) {
            return documentRepository.isPartyRelatedToCase(document.getOwnerId(), caseId);
        }
        return false;
    }

    /**
     * Get current user from Spring Security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }
        throw new RuntimeException("No authenticated user found");
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