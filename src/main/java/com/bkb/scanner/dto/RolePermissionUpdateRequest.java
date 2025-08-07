package com.bkb.scanner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class RolePermissionUpdateRequest {
    @NotNull(message = "Permissions map is required")
    private Map<String, Boolean> permissions;
}