package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString; // Import ToString
import java.util.Set;

@Entity
@Table(name = "csob_users")
@Data
@EqualsAndHashCode(callSuper = false, of = "userId") // THE FIX: Base equals/hashCode only on the ID.
@ToString(exclude = "roles") // Best practice: Exclude collections from toString to prevent issues.
public class User extends Auditable {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role; // e.g., "General Manager"

    @Column
    private String department;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "csob_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}