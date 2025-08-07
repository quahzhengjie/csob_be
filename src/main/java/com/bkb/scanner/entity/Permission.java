package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "csob_permissions")
@Data
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name; // e.g., "document:read", "document:upload"
}