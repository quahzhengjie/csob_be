package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Document;
import com.bkb.scanner.dto.DocumentSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Existing methods
    List<Document> findByOwnerTypeAndOwnerId(String ownerType, String ownerId);

    List<Document> findByOwnerTypeAndOwnerIdAndDocumentType(
            String ownerType, String ownerId, String documentType);

    @Query("SELECT d FROM Document d WHERE " +
            "(d.ownerType = 'CASE' AND d.ownerId = :caseId) OR " +
            "(d.ownerType = 'PARTY' AND d.ownerId IN " +
            "(SELECT rp.party.partyId FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId))")
    List<Document> findAllDocumentsForCase(@Param("caseId") String caseId);

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RelatedParty rp WHERE rp.party.partyId = :partyId " +
            "AND rp.ownerCase.caseId = :caseId")
    boolean isPartyRelatedToCase(@Param("partyId") String partyId,
                                 @Param("caseId") String caseId);

    // ========== NEW BATCH METHODS FOR PERFORMANCE ==========

    /**
     * Get document status summaries for multiple cases in a single query
     */
    @Query("""
        SELECT d.ownerId as caseId,
               SUM(CASE WHEN d.status = 'Expired' THEN 1 ELSE 0 END) as expiredCount,
               SUM(CASE WHEN d.status = 'Verified' THEN 1 ELSE 0 END) as verifiedCount,
               SUM(CASE WHEN d.status = 'Submitted' THEN 1 ELSE 0 END) as submittedCount,
               SUM(CASE WHEN d.status = 'Rejected' THEN 1 ELSE 0 END) as rejectedCount,
               SUM(CASE WHEN d.expiryDate IS NOT NULL 
                        AND d.expiryDate > :now 
                        AND d.expiryDate < :threshold 
                        AND d.status != 'Expired' THEN 1 ELSE 0 END) as expiringSoonCount
        FROM Document d
        WHERE d.ownerType = 'CASE' 
          AND d.ownerId IN :caseIds
          AND d.isCurrentForCase = true
        GROUP BY d.ownerId
        """)
    List<Object[]> getDocumentSummariesForCases(
            @Param("caseIds") List<String> caseIds,
            @Param("now") Instant now,
            @Param("threshold") Instant threshold
    );

    /**
     * Get all uploaded document types for multiple cases
     * Returns a map of caseId -> Set of document type names
     */
    @Query("""
        SELECT d.ownerId, d.documentType
        FROM Document d
        WHERE d.ownerType = 'CASE'
          AND d.ownerId IN :caseIds
          AND d.isCurrentForCase = true
          AND d.status IN ('Verified', 'Submitted')
        """)
    List<Object[]> findUploadedDocumentTypesByCaseIds(@Param("caseIds") List<String> caseIds);

    /**
     * Count documents by status for a specific case
     */
    @Query("""
        SELECT new com.bkb.scanner.dto.DocumentSummaryProjection(
            :caseId,
            SUM(CASE WHEN d.status = 'Expired' THEN 1 ELSE 0 END),
            SUM(CASE WHEN d.expiryDate IS NOT NULL 
                     AND d.expiryDate > :now 
                     AND d.expiryDate < :threshold 
                     AND d.status != 'Expired' THEN 1 ELSE 0 END)
        )
        FROM Document d
        WHERE d.ownerType = 'CASE' 
          AND d.ownerId = :caseId
          AND d.isCurrentForCase = true
        """)
    Optional<DocumentSummaryProjection> getDocumentSummaryForCase(
            @Param("caseId") String caseId,
            @Param("now") Instant now,
            @Param("threshold") Instant threshold
    );

    /**
     * Get count of documents per case for efficiency
     */
    @Query("""
        SELECT d.ownerId, COUNT(d)
        FROM Document d
        WHERE d.ownerType = 'CASE'
          AND d.ownerId IN :caseIds
          AND d.isCurrentForCase = true
        GROUP BY d.ownerId
        """)
    List<Object[]> countDocumentsByCaseIds(@Param("caseIds") List<String> caseIds);
}