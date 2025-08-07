package com.bkb.scanner.repository;

import com.bkb.scanner.entity.DocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing the DocumentRequirement entity.
 * This handles the rules and templates for what documents are needed.
 */
@Repository
public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, Long> {
    // This interface intentionally left blank.
    // Spring Data JPA will automatically provide all the necessary database methods.
}