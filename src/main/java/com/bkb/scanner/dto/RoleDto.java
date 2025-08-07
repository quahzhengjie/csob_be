// =================================================================================
// 4. UPDATE RoleDto.java - Add ID field
// =================================================================================
package com.bkb.scanner.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RoleDto {
    private Long id; // ADD THIS FIELD
    private String name;
    private String label;
    private Map<String, Boolean> permissions;
}