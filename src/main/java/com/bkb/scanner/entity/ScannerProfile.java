package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "csob_scanner_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
public class ScannerProfile extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String resolution; // e.g., "300dpi"
    private String colorMode;  // e.g., "Color", "Grayscale"
    private String source;     // e.g., "ADF", "Flatbed"
    private boolean isDefault;
}