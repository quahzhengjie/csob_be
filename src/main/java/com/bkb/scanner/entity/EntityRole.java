package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
@Entity
@Table(name = "csob_entity_roles")
@Data
@EqualsAndHashCode(callSuper = false, of = {"id"})
@SQLDelete(sql = "UPDATE csob_entity_roles SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "is_deleted = false")
public class EntityRole extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String roleName;

    @Column(nullable = false)
    private int priority = 1; // NEW

    @Column(nullable = false)
    private int displayOrder = 0; // Keep this for backward compatibility
}