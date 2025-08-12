// src/main/java/com/bkb/scanner/dto/ChecklistDocumentDto.java
package com.bkb.scanner.dto;

import com.bkb.scanner.entity.Document;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChecklistDocumentDto {
    private String id;
    private Long documentId;
    private String name;
    private boolean required;
    private String description;
    private Integer validityMonths;
    private String category;
    private String status;
    private String ownerId;
    private String ownerName;
    private Integer version;
    private String uploadedDate;
    private String expiryDate;
    private String mimeType;
    private String rejectionReason;
    private String comments;
    private DocumentDto.UserInfoDto uploadedBy;
    private DocumentDto.UserInfoDto verifiedBy;
    private String verifiedDate;
    private boolean isAdHoc;
    private List<DocumentDto> allVersions;
}