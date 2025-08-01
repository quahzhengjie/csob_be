package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;

@Entity
@Table(name = "csob_call_reports")
@Data
@EqualsAndHashCode(callSuper = false)
public class CallReport extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant callDate;
    @Lob
    private String summary;
    @Lob
    private String nextSteps;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private Case ownerCase;
}