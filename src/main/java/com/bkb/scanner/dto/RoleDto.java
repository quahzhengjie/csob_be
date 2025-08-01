package com.bkb.scanner.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RoleDto {
    private String name;
    private String label;
    private Map<String, Boolean> permissions; // Changed from Set<String> to Map<String, Boolean>
}