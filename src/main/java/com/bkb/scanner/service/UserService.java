package com.bkb.scanner.service;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.entity.User;
import com.bkb.scanner.repository.RoleRepository;
import com.bkb.scanner.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Converts a User entity to a UserDto for sending to the frontend.
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setDepartment(user.getDepartment());

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Role primaryRole = user.getRoles().iterator().next();
            dto.setRoleId(primaryRole.getId());
            dto.setRole(primaryRole.getLabel());
            dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        }
        return dto;
    }

    /**
     * Generates the next sequential user ID (e.g., USER-006).
     */
    private String getNextUserId() {
        User lastUser = userRepository.findTopByOrderByUserIdDesc();
        if (lastUser == null) {
            return "USER-001"; // This is the first user in the system.
        }
        String lastId = lastUser.getUserId();
        int lastNumber = Integer.parseInt(lastId.substring(5));
        int newNumber = lastNumber + 1;
        return String.format("USER-%03d", newNumber);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(String userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    @Transactional
    public UserDto createUser(UserCreationRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + request.getRoleId()));

        User newUser = new User();

        newUser.setUserId(getNextUserId());
        newUser.setUsername(request.getEmail().split("@")[0]);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setDepartment(request.getDepartment());
        newUser.setEnabled(true);

        newUser.setRole(role.getLabel());
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        newUser.setRoles(roles);

        User savedUser = userRepository.save(newUser);
        log.info("Created new user: {} with role: {}", savedUser.getUserId(), role.getName());
        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());

        // ** THE FIX IS HERE **
        // Check if a new password was provided in the request.
        // The isBlank() check ensures we don't update to an empty password.
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Password has been reset for user {}", userId);
        }

        // Check for and update the role if it has changed
        Long currentRoleId = user.getRoles().isEmpty() ? null : user.getRoles().iterator().next().getId();
        if (request.getRoleId() != null && !request.getRoleId().equals(currentRoleId)) {
            Role newRole = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + request.getRoleId()));
            user.setRole(newRole.getLabel());
            user.getRoles().clear();
            user.getRoles().add(newRole);
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Transactional
    public UserDto updateUserStatus(String userId, boolean isEnabled) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        userRepository.setUserEnabledState(userId, isEnabled);

        User updatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Failed to refetch user after status update: " + userId));

        log.info("Updated user {} status to: {}", userId, isEnabled ? "enabled" : "disabled");
        return convertToDto(updatedUser);
    }

    /**
     * Soft delete a user
     * Sets is_deleted=true and deleted_at=current timestamp
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Perform soft delete
        user.setDeleted(true);
        user.setDeletedAt(Instant.now()); // Changed from LocalDateTime.now()
        user.setEnabled(false); // Also disable the user

        userRepository.save(user);
        log.info("Soft deleted user: {}", userId);
    }

    /**
     * Update password for a user (used by /me/password endpoint)
     * This is for users changing their own password (requires current password verification)
     */
    @Transactional
    public void updatePassword(String username, PasswordUpdateDto passwordUpdate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Verify current password
        if (!passwordEncoder.matches(passwordUpdate.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(passwordUpdate.getNewPassword()));
        userRepository.save(user);

        log.info("Password updated for user: {}", username);
    }

    @Transactional
    public List<UserDto> batchUpdateUserRoles(List<BatchUserRoleUpdateRequest.UserRoleUpdate> updates) {
        List<UserDto> updatedUsers = new ArrayList<>();
        for (BatchUserRoleUpdateRequest.UserRoleUpdate update : updates) {
            try {
                User user = userRepository.findById(update.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found: " + update.getUserId()));

                Role newRole = roleRepository.findByName(update.getRole())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + update.getRole()));

                user.getRoles().clear();
                user.getRoles().add(newRole);
                user.setRole(newRole.getLabel());

                User savedUser = userRepository.save(user);
                updatedUsers.add(convertToDto(savedUser));
                log.info("Updated role for user {} to {}", update.getUserId(), update.getRole());
            } catch (Exception e) {
                log.error("Failed to update role for user {}: {}", update.getUserId(), e.getMessage());
            }
        }
        return updatedUsers;
    }
}