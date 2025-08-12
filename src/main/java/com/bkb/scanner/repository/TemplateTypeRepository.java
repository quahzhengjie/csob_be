package com.bkb.scanner.repository;

import com.bkb.scanner.entity.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateTypeRepository extends JpaRepository<TemplateType, Long> {

    /**
     * Finds a specific template type by its display name within a given category key.
     * This is used to uniquely identify a template for updates.
     * For example, find where displayName = "Non-Listed Company" and category.categoryKey = "entityTemplates".
     */
    Optional<TemplateType> findByDisplayNameAndCategory_CategoryKey(String displayName, String categoryKey);

    /**
     * Finds a specific template type by its type key within a given category key.
     * This is used to uniquely identify a template for updates.
     * For example, find where typeKey = "Non-Listed Company" and category.categoryKey = "entityTemplates".
     */
    Optional<TemplateType> findByTypeKeyAndCategory_CategoryKey(String typeKey, String categoryKey);
}