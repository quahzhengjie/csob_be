package com.bkb.scanner.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String jwt;
    private String username;
    private String userId;
    private String name;
    private String email;
    private String role;
    private String roleLabel;
    private Set<String> permissions;
    private Long expiresIn; // Token expiration time in seconds
    private String department;
}