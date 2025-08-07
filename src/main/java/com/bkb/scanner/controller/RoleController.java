// =================================================================================
// 1. UPDATE RoleController.java - Complete file with all endpoints
// =================================================================================
package com.bkb.scanner.controller;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<Map<String, RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleName}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String roleName) {
        return roleService.getRoleByName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleCreationRequest request) {
        return new ResponseEntity<>(roleService.createRole(request), HttpStatus.CREATED);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(roleService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{roleName}/permissions")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<RoleDto> updateRolePermissions(
            @PathVariable String roleName,
            @Valid @RequestBody RolePermissionUpdateRequest request) {
        return ResponseEntity.ok(roleService.updateRolePermissions(roleName, request.getPermissions()));
    }
}