package com.bkb.scanner.dto;

import lombok.Data;

/**
 * DTO for entity data embedded in a case
 */
@Data
public class EntityDataDto {
    private String customerId;
    private String entityName;
    private String entityType;
    private String basicNumber;
    private String cisNumber;
    private String taxId;
    private String address1;
    private String address2;
    private String addressCountry;
    private String placeOfIncorporation;
    private String usFatcaClassificationFinal;
    private CreditDetailsDto creditDetails;
}