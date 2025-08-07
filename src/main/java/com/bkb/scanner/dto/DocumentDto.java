package com.bkb.scanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {
    private Long id;
    private String name;
    private String documentType;
    private String originalFilename;
    private String mimeType;
    private Long sizeInBytes;
    private String status;
    private Integer version;
    private String ownerType;
    private String ownerId;

    // User details for uploaded by
    private UserInfoDto uploadedBy;
    private String uploadedDate;

    // User details for verified by
    private UserInfoDto verifiedBy;
    private String verifiedDate;

    private String rejectionReason;
    private String expiryDate;
    private String comments;
    private Boolean isCurrentForCase;
    private Boolean isAdHoc;

    // Nested DTO for user information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private String userId;
        private String username;
        private String name;
        private String department;
    }
}