package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "csob_bank_forms")
@Data
@EqualsAndHashCode(callSuper = false, of = {"id"})
@SQLDelete(sql = "UPDATE csob_bank_forms SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
public class BankForm extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g., "corporateMandatory", "corporateOptional"

    @Column(nullable = false)
    private String name; // CHANGED from formName

    @Column(nullable = false)
    private String formCode; // NEW: e.g., "SC-001"

    @Column(nullable = false)
    private boolean isMandatory = true; // NEW

    @Column(name = "applicable_entity_types")
    private String applicableEntityTypes = "ALL"; // NEW: e.g., "ALL" or "Corporate,Trust"

    @Column(nullable = false)
    private int displayOrder = 0;
}