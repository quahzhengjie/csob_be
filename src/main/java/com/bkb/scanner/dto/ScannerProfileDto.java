package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScannerProfileDto {
    private Long id;
    @NotBlank
    private String name;
    private String resolution;
    private String colorMode;
    private String source;
    private boolean isDefault;
}