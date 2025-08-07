// =================================================================================
// 3. CREATE RoleUpdateRequest.java
// =================================================================================
package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotBlank(message = "Role label is required")
    private String label;
}
