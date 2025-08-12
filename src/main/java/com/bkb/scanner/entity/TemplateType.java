package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.util.List;

@Entity
@Table(name = "csob_template_types")
@Data
@EqualsAndHashCode(callSuper = false, of = {"id"})
// ** THE FIX IS HERE: Add soft-delete annotations **
@SQLDelete(sql = "UPDATE csob_template_types SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
public class TemplateType extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TemplateCategory category;

    @Column(nullable = false)
    private String typeKey;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private int displayOrder = 0;

    @OneToMany(mappedBy = "templateType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    private List<DocumentRequirement> documentRequirements;
}