package com.bkb.scanner.repository;

import com.bkb.scanner.entity.EntityRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntityRoleRepository extends JpaRepository<EntityRole, Long> {
    List<EntityRole> findByEntityTypeOrderByPriority(String entityType);
}