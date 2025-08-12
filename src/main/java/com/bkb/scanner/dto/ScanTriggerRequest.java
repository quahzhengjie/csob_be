package com.bkb.scanner.dto;

import lombok.Data;

@Data
public class ScanTriggerRequest {
    private String profileName;
    private String ownerType;
    private String ownerId;
    private String documentType;
    private String format; // Add format field (pdf or png)
}