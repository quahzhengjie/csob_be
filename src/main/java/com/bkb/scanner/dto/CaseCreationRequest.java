// FILE: src/main/java/com/bkb/scanner/dto/CaseCreationRequest.java
package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CaseCreationRequest {
    @NotBlank
    private String entityName;

    @NotBlank
    private String entityType;

    @NotNull
    private String riskLevel;

    @NotNull
    private String status;

    // Add nested entity details
    @Valid
    private EntityDetails entity;

    @Data
    public static class EntityDetails {
        @NotBlank(message = "Basic Number is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "Basic Number must be exactly 6 digits")
        private String basicNumber;

        // All other fields are optional
        private String cisNumber;
        private String taxId;
        private String address1;
        private String address2;
        private String addressCountry;
        private String placeOfIncorporation;
        private String businessActivity;
        private String contactPerson;
        private String contactEmail;
        private String contactPhone;
    }
}