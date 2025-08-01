package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find all documents for a specific owner (case or party)
     */
    List<Document> findByOwnerTypeAndOwnerId(String ownerType, String ownerId);

    /**
     * Find all documents for a case
     */
    default List<Document> findByCaseId(String caseId) {
        return findByOwnerTypeAndOwnerId("CASE", caseId);
    }

    /**
     * Find all documents for a party
     */
    default List<Document> findByPartyId(String partyId) {
        return findByOwnerTypeAndOwnerId("PARTY", partyId);
    }

    /**
     * Find documents by type for a specific owner
     */
    List<Document> findByOwnerTypeAndOwnerIdAndDocumentType(String ownerType, String ownerId, String documentType);

    /**
     * Find the latest version of a document
     */
    @Query("SELECT d FROM Document d WHERE d.ownerType = :ownerType AND d.ownerId = :ownerId " +
            "AND d.documentType = :documentType ORDER BY d.version DESC")
    List<Document> findLatestVersion(@Param("ownerType") String ownerType,
                                     @Param("ownerId") String ownerId,
                                     @Param("documentType") String documentType);

    /**
     * Count documents by status for a specific owner
     */
    Long countByOwnerTypeAndOwnerIdAndStatus(String ownerType, String ownerId, String status);

    /**
     * Find expired documents
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate < CURRENT_TIMESTAMP AND d.status != 'Expired'")
    List<Document> findExpiredDocuments();

    /**
     * Find all documents related to a case (including party documents)
     */
    @Query("SELECT d FROM Document d WHERE " +
            "(d.ownerType = 'CASE' AND d.ownerId = :caseId) OR " +
            "(d.ownerType = 'PARTY' AND d.ownerId IN " +
            "   (SELECT rp.party.partyId FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId))")
    List<Document> findAllDocumentsForCase(@Param("caseId") String caseId);

    /**
     * ✅ NEW METHOD: Check if a party is related to a case
     * This is needed for the documentBelongsToCase validation
     */
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RelatedParty rp WHERE rp.party.partyId = :partyId AND rp.ownerCase.caseId = :caseId")
    boolean isPartyRelatedToCase(@Param("partyId") String partyId, @Param("caseId") String caseId);

    /**
     * ✅ NEW METHOD: Clear current flags for a document type
     * This method updates all documents of a specific type to not be current
     * Used when uploading a new version or making a different version current
     */
    @Modifying
    @Query("UPDATE Document d SET d.isCurrentForCase = false " +
            "WHERE d.documentType = :documentType AND " +
            "((d.ownerType = 'CASE' AND d.ownerId = :caseId) OR " +
            "(d.ownerType = 'PARTY' AND d.ownerId IN " +
            "(SELECT rp.party.partyId FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId)))")
    void clearCurrentFlagsForDocumentType(@Param("caseId") String caseId, @Param("documentType") String documentType);

    /**
     * ✅ NEW METHOD: Find all versions of a document type for a case
     * This includes both case documents and related party documents
     */
    @Query("SELECT d FROM Document d WHERE d.documentType = :documentType AND " +
            "((d.ownerType = 'CASE' AND d.ownerId = :caseId) OR " +
            "(d.ownerType = 'PARTY' AND d.ownerId IN " +
            "(SELECT rp.party.partyId FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId))) " +
            "ORDER BY d.version DESC")
    List<Document> findAllVersionsForCaseAndDocumentType(@Param("caseId") String caseId,
                                                         @Param("documentType") String documentType);

    /**
     * ✅ NEW METHOD: Find the current version of a document type for a case
     */
    @Query("SELECT d FROM Document d WHERE d.documentType = :documentType " +
            "AND d.isCurrentForCase = true AND " +
            "((d.ownerType = 'CASE' AND d.ownerId = :caseId) OR " +
            "(d.ownerType = 'PARTY' AND d.ownerId IN " +
            "(SELECT rp.party.partyId FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId)))")
    List<Document> findCurrentDocumentForCase(@Param("caseId") String caseId,
                                              @Param("documentType") String documentType);

    /**
     * ✅ NEW METHOD: Update isCurrentForCase flag for a specific document
     */
    @Modifying
    @Query("UPDATE Document d SET d.isCurrentForCase = :isCurrent WHERE d.id = :documentId")
    void updateIsCurrentFlag(@Param("documentId") Long documentId, @Param("isCurrent") boolean isCurrent);
}