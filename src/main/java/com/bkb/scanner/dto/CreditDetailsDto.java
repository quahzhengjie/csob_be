
package com.bkb.scanner.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreditDetailsDto {
    private BigDecimal creditLimit;
    private String creditScore;
    private String assessmentNotes;
}