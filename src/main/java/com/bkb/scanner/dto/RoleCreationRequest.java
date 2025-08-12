// =================================================================================
// 2. CREATE RoleCreationRequest.java
// =================================================================================
package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.Map;

@Data
public class RoleCreationRequest {
    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "Role name must start with ROLE_ and contain only uppercase letters and underscores")
    private String name;

    @NotBlank(message = "Role label is required")
    private String label;

    private Map<String, Boolean> permissions;
}