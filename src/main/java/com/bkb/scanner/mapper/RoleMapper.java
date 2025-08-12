
// =================================================================================
// 6. UPDATE RoleMapper.java - Add ID mapping
// =================================================================================
package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.RoleDto;
import com.bkb.scanner.entity.Permission;
import com.bkb.scanner.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions", expression = "java(mapPermissionsToMap(role.getPermissions()))")
    @Mapping(target = "id", source = "id") // ADD THIS MAPPING
    RoleDto toDto(Role role);

    default Map<String, Boolean> mapPermissionsToMap(Set<Permission> permissions) {
        if (permissions == null) {
            return Map.of();
        }

        // Create a map with all permissions set to true
        return permissions.stream()
                .collect(Collectors.toMap(
                        Permission::getName,
                        permission -> true
                ));
    }
}
