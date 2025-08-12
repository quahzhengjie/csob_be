package com.bkb.scanner.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for party with associated documents
 */
@Data
public class PartyWithDocumentsDto {
    private PartyDto party;
    private List<DocumentDto> documents;
}