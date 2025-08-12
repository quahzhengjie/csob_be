package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Role;
import com.bkb.scanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    long countByRolesContaining(Role role);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Finds the single user with the highest userId lexicographically
     * (e.g., USER-005 > USER-004), used for generating the next ID.
     */
    User findTopByOrderByUserIdDesc();

    /**
     * This query directly updates the 'enabled' status in the database,
     * bypassing any potential issues with Hibernate's dirty checking.
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :isEnabled WHERE u.userId = :userId")
    void setUserEnabledState(@Param("userId") String userId, @Param("isEnabled") boolean isEnabled);
}