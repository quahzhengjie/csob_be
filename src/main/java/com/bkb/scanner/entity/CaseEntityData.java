package com.bkb.scanner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;

/**
 * Embeddable class containing entity data for a case
 */
@Embeddable
@Data
public class CaseEntityData {
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "basic_number")
    private String basicNumber;

    @Column(name = "cis_number")
    private String cisNumber;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "address_country")
    private String addressCountry;

    @Column(name = "place_of_incorporation")
    private String placeOfIncorporation;

    @Column(name = "us_fatca_classification_final")
    private String usFatcaClassificationFinal;

    @Embedded
    private CreditDetails creditDetails;
}