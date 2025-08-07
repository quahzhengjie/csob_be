package com.bkb.scanner.dto;

import lombok.Data;

@Data
public class BankFormDto {
    private Long id;
    private String category;
    private String name;
    private String formCode;
    private boolean isMandatory;
    private String applicableEntityTypes;
    private int displayOrder;
}