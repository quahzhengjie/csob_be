package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "csob_kyc_configurations")
@Data
public class KycConfiguration {
    @Id
    private String configKey;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String configValue;
}