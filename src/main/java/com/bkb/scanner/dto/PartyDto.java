package com.bkb.scanner.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for party information
 */
@Data
public class PartyDto {
    private String partyId;
    private String name;
    private String firstName;
    private String lastName;
    private String residencyStatus;
    private String idType;
    private String identityNo;
    private LocalDate birthDate;
    private String employmentStatus;
    private String employerName;
    private boolean isPEP;
    private String pepCountry;
}