package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Set<Role> findByNameIn(Set<String> names);

    Optional<Role> findByName(String name);

    // Override findAll to ensure permissions are eagerly loaded
    @Override
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAll();

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
}