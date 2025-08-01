package com.bkb.scanner.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class DocumentDto {
    private Long id;
    private String documentType;
    private String originalFilename;
    private String mimeType;
    private long sizeInBytes;
    private Instant expiryDate;
    private String createdBy;
    private Instant createdDate;

    // --- NEW FIELDS TO MATCH FRONTEND ---
    private String name; // The document type name, e.g., "Passport"
    private String status;
    private Integer version;
    private String ownerType;
    private String ownerId;
    private String uploadedBy;
    private String verifiedBy;
    private Instant verifiedDate;
    private String rejectionReason;
    private String comments;

    // ✅ ADD THIS NEW FIELD
    private Boolean isCurrentForCase;
}