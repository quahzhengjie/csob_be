package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RelatedPartyDto {
    private Long id;
    @NotBlank
    private String partyId;
    @NotBlank
    private String name;
    @NotBlank
    private String relationshipType;
    private Double ownershipPercentage;
}