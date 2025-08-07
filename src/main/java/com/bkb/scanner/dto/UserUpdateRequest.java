package com.bkb.scanner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private String department;

    // This field will receive the new password, if provided.
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}