package com.bkb.scanner.dto;

import lombok.Data;

@Data
public class ScanTriggerResponse {
    private String documentId;
    private String status;
    private String message;
}