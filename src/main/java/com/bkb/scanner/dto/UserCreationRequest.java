package com.bkb.scanner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class UserCreationRequest {
    @NotEmpty
    private String userId;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private boolean enabled = true;

    @NotEmpty
    private Set<String> roles;

    // --- NEW FIELDS TO MATCH FRONTEND ---
    @NotEmpty
    private String name;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String role; // Primary role label

    private String department;
}