package com.bkb.scanner.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO representing the link between a case and a party, including their relationships.
 * This is different from RelatedPartyDto which represents the party details.
 */
@Data
public class RelatedPartyLinkDto {
    private String partyId;
    private List<RelationshipDto> relationships;

    @Data
    public static class RelationshipDto {
        private String type;
        private Double ownershipPercentage;
    }
}