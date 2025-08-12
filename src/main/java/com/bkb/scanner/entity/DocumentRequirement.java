// DocumentRequirement.java

package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "csob_document_requirements")
@Data
@EqualsAndHashCode(callSuper = false, of = {"id"})
@SQLDelete(sql = "UPDATE csob_document_requirements SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
public class DocumentRequirement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_type_id", nullable = false)
    private TemplateType templateType;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * NEW: Category to group documents.
     * Values: 'CUSTOMER', 'BANK_MANDATORY', 'BANK_NON_MANDATORY'
     */
    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean required = true;

    private Integer validityMonths;

    @Column(nullable = false)
    private int displayOrder = 0;
}