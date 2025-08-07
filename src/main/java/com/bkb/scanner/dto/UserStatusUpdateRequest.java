package com.bkb.scanner.dto;

import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    // FIX: Renamed from isEnabled to enabled to follow standard Java Bean conventions,
    // which resolves JSON deserialization issues with Jackson.
    private boolean enabled;
}