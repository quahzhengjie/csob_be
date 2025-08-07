
// =================================================================================
// 5. UPDATE RoleService.java - Complete file with all methods
// =================================================================================
package com.bkb.scanner.service;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.Permission;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.exception.ConflictException;
import com.bkb.scanner.exception.NotFoundException;
import com.bkb.scanner.mapper.RoleMapper;
import com.bkb.scanner.repository.PermissionRepository;
import com.bkb.scanner.repository.RoleRepository;
import com.bkb.scanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    // System roles that cannot be deleted
    private static final Set<String> SYSTEM_ROLES = Set.of(
            "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_PROCESSOR", "ROLE_VIEWER", "ROLE_COMPLIANCE"
    );

    public Map<String, RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .collect(Collectors.toMap(
                        Role::getName,
                        roleMapper::toDto,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public Optional<RoleDto> getRoleByName(String roleName) {
        return roleRepository.findByNameWithPermissions(roleName)
                .map(roleMapper::toDto);
    }

    @Transactional
    public RoleDto createRole(RoleCreationRequest request) {
        // Check if role already exists
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException("Role already exists: " + request.getName());
        }

        Role newRole = new Role();
        newRole.setName(request.getName());
        newRole.setLabel(request.getLabel());

        // Set permissions if provided
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();

            for (Map.Entry<String, Boolean> entry : request.getPermissions().entrySet()) {
                if (entry.getValue()) {
                    Permission permission = permissionRepository.findByName(entry.getKey())
                            .orElseGet(() -> {
                                Permission newPermission = new Permission();
                                newPermission.setName(entry.getKey());
                                return permissionRepository.save(newPermission);
                            });
                    permissions.add(permission);
                }
            }

            newRole.setPermissions(permissions);
        } else {
            newRole.setPermissions(new HashSet<>());
        }

        Role savedRole = roleRepository.save(newRole);
        log.info("Created new role: {} with label: {}", savedRole.getName(), savedRole.getLabel());

        return roleMapper.toDto(savedRole);
    }

    @Transactional
    public RoleDto updateRole(Long roleId, RoleUpdateRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

        role.setLabel(request.getLabel());
        Role updatedRole = roleRepository.save(role);

        log.info("Updated role {} label to: {}", role.getName(), request.getLabel());

        return roleMapper.toDto(updatedRole);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId));

        // Check if it's a system role
        if (SYSTEM_ROLES.contains(role.getName())) {
            throw new ConflictException("Cannot delete system role: " + role.getName());
        }

        // Check if any users have this role
        long userCount = userRepository.countByRolesContaining(role);
        if (userCount > 0) {
            throw new ConflictException("Cannot delete role '" + role.getName() +
                    "' as it is assigned to " + userCount + " user(s)");
        }

        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getName());
    }

    @Transactional
    public RoleDto updateRolePermissions(String roleName, Map<String, Boolean> permissionUpdates) {
        Role role = roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        // Get all existing permissions for this role
        Set<Permission> currentPermissions = new HashSet<>(role.getPermissions());

        // Process permission updates
        for (Map.Entry<String, Boolean> entry : permissionUpdates.entrySet()) {
            String permissionName = entry.getKey();
            boolean shouldHave = entry.getValue();

            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseGet(() -> {
                        // Create new permission if it doesn't exist
                        Permission newPermission = new Permission();
                        newPermission.setName(permissionName);
                        return permissionRepository.save(newPermission);
                    });

            if (shouldHave) {
                currentPermissions.add(permission);
            } else {
                currentPermissions.remove(permission);
            }
        }

        // Update the role's permissions
        role.setPermissions(currentPermissions);
        Role updatedRole = roleRepository.save(role);

        log.info("Updated permissions for role {}: {}", roleName,
                currentPermissions.stream().map(Permission::getName).collect(Collectors.toList()));

        return roleMapper.toDto(updatedRole);
    }
}