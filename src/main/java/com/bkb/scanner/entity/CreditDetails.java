package com.bkb.scanner.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.math.BigDecimal;

@Embeddable
@Data
public class CreditDetails {
    private BigDecimal creditLimit;
    private String creditScore;
    private String assessmentNotes;
}