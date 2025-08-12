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

    @Column(nullable = false)
    private String name;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_in_bytes")
    private long sizeInBytes;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @Column(columnDefinition="LONGBLOB")
    private byte[] content;

    @Column(name = "owner_type", nullable = false)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case ownerCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party ownerParty;

    @Column
    private String status;

    @Column
    private Integer version = 1;

    // --- REFACTORED FIELDS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", referencedColumnName = "user_id", nullable = false)
    private User uploadedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id", referencedColumnName = "user_id")
    private User verifiedByUser;
    // --- END REFACTOR ---

    @Column(name = "verified_date")
    private Instant verifiedDate;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Lob
    private String comments;

    @Column(name = "is_current_for_case", nullable = false)
    private Boolean isCurrentForCase = false;

    @Column(name = "is_ad_hoc", nullable = false)
    private Boolean isAdHoc = false;
}