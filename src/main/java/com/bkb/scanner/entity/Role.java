package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "csob_roles")
@Data
@EqualsAndHashCode(callSuper = false)
public class Role extends Auditable { // CORRECTED: Extends Auditable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "ROLE_MANAGER"

    @Column(nullable = false)
    private String label; // e.g., "Manager"

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "csob_role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
