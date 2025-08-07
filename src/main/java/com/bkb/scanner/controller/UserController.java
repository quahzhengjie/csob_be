package com.bkb.scanner.controller;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get all users - requires admin permission
     */
    @GetMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("=== GET ALL USERS REQUEST ===");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("🔐 Current user: {}", auth.getName());
            logger.info("🔐 Authorities: {}", auth.getAuthorities());
        }

        try {
            List<UserDto> users = userService.findAllUsers();
            logger.info("✅ Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error retrieving users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get basic user information accessible to any authenticated user with case:read permission.
     * This endpoint returns only non-sensitive user data needed for display purposes
     * (e.g., showing who a case is assigned to, activity logs, etc.)
     */
    @GetMapping("/basic")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<BasicUserDto>> getBasicUsers() {
        logger.info("=== GET BASIC USERS REQUEST ===");

        // Log who's making the request
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("🔐 Current user: {}", auth.getName());
            logger.info("🔐 Authorities: {}", auth.getAuthorities());
        }

        try {
            // Get all users from service and convert to basic DTOs
            List<BasicUserDto> basicUsers = userService.findAllUsers().stream()
                    .filter(UserDto::isEnabled) // Only return active users
                    .map(user -> BasicUserDto.builder()
                            .userId(user.getUserId())
                            .name(user.getName())
                            .department(user.getDepartment())
                            .build())
                    .collect(Collectors.toList());

            logger.info("✅ Successfully retrieved {} basic users", basicUsers.size());
            return ResponseEntity.ok(basicUsers);
        } catch (Exception e) {
            logger.error("❌ Error retrieving basic users: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get user by ID - requires admin permission
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        logger.info("Getting user by ID: {}", userId);
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new user - requires admin permission
     */
    @PostMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());
        try {
            UserDto createdUser = userService.createUser(request);
            logger.info("✅ Successfully created user: {}", createdUser.getUserId());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Error creating user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update user - requires admin permission
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        logger.info("Updating user: {}", userId);
        try {
            UserDto updatedUser = userService.updateUser(userId, request);
            logger.info("✅ Successfully updated user: {}", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("❌ Error updating user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update user (PATCH) - requires admin permission
     * This is the existing PATCH endpoint in your code
     */
    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> patchUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    /**
     * Update user status (enable/disable) - requires admin permission
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        logger.info("Updating user status - userId: {}, enabled: {}", userId, request.isEnabled());
        try {
            UserDto updatedUser = userService.updateUserStatus(userId, request.isEnabled());
            logger.info("✅ Successfully updated user status for: {}", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("❌ Error updating user status: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Delete user (soft delete) - requires admin permission
     * Sets is_deleted=true and deleted_at=current_timestamp
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        logger.info("Soft deleting user: {}", userId);
        try {
            userService.deleteUser(userId); // This performs a soft delete
            logger.info("✅ Successfully soft deleted user: {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ Error soft deleting user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Batch update user roles - requires admin permission
     */
    @PostMapping("/batch-update-roles")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<List<UserDto>> batchUpdateUserRoles(
            @Valid @RequestBody BatchUserRoleUpdateRequest request) {
        return ResponseEntity.ok(userService.batchUpdateUserRoles(request.getUpdates()));
    }

    /**
     * Get current user's profile - any authenticated user can access their own profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            logger.info("Getting profile for current user: {}", auth.getName());
            return userService.getUserByUsername(auth.getName())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Update current user's password - any authenticated user can update their own password
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateMyPassword(@RequestBody @Valid PasswordUpdateDto passwordUpdate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            logger.info("Updating password for current user: {}", auth.getName());
            try {
                userService.updatePassword(auth.getName(), passwordUpdate);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                logger.error("❌ Error updating password: {}", e.getMessage());
                throw e;
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

