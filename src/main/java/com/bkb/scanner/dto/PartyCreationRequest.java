package com.bkb.scanner.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PartyCreationRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "Residency status is required")
    private String residencyStatus;
    @NotBlank(message = "ID type is required")
    private String idType;
    @NotBlank(message = "Identity number is required")
    private String identityNo;
    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;
    private String employmentStatus;
    private String employerName;
    private boolean isPEP;
    private String pepCountry;
}