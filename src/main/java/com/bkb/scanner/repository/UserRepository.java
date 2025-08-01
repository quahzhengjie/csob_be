package com.bkb.scanner.repository;

import com.bkb.scanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Finds a user by their username and eagerly fetches their associated roles and permissions.
     *
     * This custom query is crucial for the security context. It uses 'LEFT JOIN FETCH'
     * to ensure that when a User is loaded, all of their Role and Permission entities
     * are loaded in the same query. This prevents LazyInitializationException errors
     * and ensures the UserDetails object is fully populated.
     *
     * @param username The username of the user to find.
     * @return An Optional containing the User with fully initialized roles and permissions, if found.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
}
