package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "csob_users")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends Auditable {
    @Id
    @Column(name = "user_id") // Added column name for clarity
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    // --- NEW FIELDS ---
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // e.g., "General Manager"

    @Column
    private String department;
    // --- END NEW FIELDS ---

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "csob_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}