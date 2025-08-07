package com.bkb.scanner.repository;

import com.bkb.scanner.entity.TemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateCategoryRepository extends JpaRepository<TemplateCategory, Long> {
}