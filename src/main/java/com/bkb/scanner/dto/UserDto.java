package com.bkb.scanner.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private String userId;
    private String username;
    private boolean enabled;
    private Set<String> roles; // Corresponds to the Set<Role> in the entity

    // --- NEW FIELDS TO MATCH FRONTEND ---
    private String name;
    private String email;
    private String role; // The primary role label, e.g., "General Manager"
    private String department;
}