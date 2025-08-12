package com.bkb.scanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchUserRoleUpdateRequest {
    @NotEmpty(message = "Updates list cannot be empty")
    @Valid
    private List<UserRoleUpdate> updates;

    @Data
    public static class UserRoleUpdate {
        @NotBlank(message = "User ID is required")
        private String userId;

        @NotBlank(message = "Role is required")
        private String role;
    }
}