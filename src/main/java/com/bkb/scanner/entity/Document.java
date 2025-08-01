package com.bkb.scanner.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;

@Entity
@Table(name = "csob_documents")
@Data
@EqualsAndHashCode(callSuper = false)
public class Document extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- NEW FIELDS ---
    @Column(nullable = false)
    private String name; // The document type name, e.g., "Passport"

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_date")
    private Instant verifiedDate;

    @Lob
    private String comments;

    // ✅ ADD THIS NEW FIELD
    @Column(name = "is_current_for_case", nullable = false)
    private Boolean isCurrentForCase = false;
    // --- END NEW FIELDS ---

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_in_bytes")
    private long sizeInBytes;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "owner_type", nullable = false)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column
    private Integer version = 1;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @Column(columnDefinition="LONGBLOB") // Or VARBINARY(MAX) for MSSQL
    private byte[] content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case ownerCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party ownerParty;
}