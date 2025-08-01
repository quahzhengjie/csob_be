package com.bkb.scanner.service;

import com.bkb.scanner.dto.EnumConfigDto;
import com.bkb.scanner.dto.RoleDto;
import com.bkb.scanner.entity.Permission;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.mapper.RoleMapper;
import com.bkb.scanner.repository.RoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnumService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private KycConfigurationService kycConfigurationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public EnumConfigDto getEnumConfig() {
        EnumConfigDto config = new EnumConfigDto();

        // Fetch roles with permissions eagerly loaded
        List<Role> roles = roleRepository.findAll();

        // Manual mapping to ensure permissions are included
        Map<String, RoleDto> roleMap = new HashMap<>();

        for (Role role : roles) {
            RoleDto roleDto = new RoleDto();
            roleDto.setName(role.getName());
            roleDto.setLabel(role.getLabel());

            // Map permissions to a Map<String, Boolean>
            Map<String, Boolean> permissionMap = new HashMap<>();
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    permissionMap.put(permission.getName(), true);
                }
            }
            roleDto.setPermissions(permissionMap);

            roleMap.put(role.getName(), roleDto);

            // Debug logging
            System.out.println("Mapped role: " + role.getName() + " with " + permissionMap.size() + " permissions");
        }

        config.setRoles(roleMap);

        // Dynamically get the entity types from the configuration
        List<String> entityTypes = getEntityTypesFromConfig();

        // Define the enums with the dynamic list
        Map<String, List<String>> enums = Map.of(
                "caseStatus", List.of("Prospect", "KYC Review", "Pending Approval", "Active", "Rejected"),
                "riskLevel", List.of("Low", "Medium", "High"),
                "docStatus", List.of("Missing", "Submitted", "Verified", "Rejected", "Expired"),
                "entityTypes", entityTypes
        );
        config.setEnums(enums);

        return config;
    }

    // Helper method to parse the JSON and extract the keys
    private List<String> getEntityTypesFromConfig() {
        try {
            String configJson = kycConfigurationService.getDocumentRequirementsTemplate();
            JsonNode rootNode = objectMapper.readTree(configJson);
            JsonNode entityTemplatesNode = rootNode.path("entityTemplates");

            List<String> types = new ArrayList<>();
            entityTemplatesNode.fieldNames().forEachRemaining(types::add);
            return types;
        } catch (IOException e) {
            // Log the error
            e.printStackTrace();

            // Return the full, comprehensive list as a fallback
            return List.of(
                    "Non-Listed Company",
                    "Joint Account",
                    "Joint Account (Non-resident)",
                    "Partnership",
                    "Sole Proprietorship",
                    "Societies/MCST",
                    "Trust Account",
                    "Listed Company",
                    "Complex Corporation",
                    "Local Regulated Company",
                    "Foundation",
                    "Non-Profit Organization",
                    "Bank",
                    "Foreign Govt. Organization"
            );
        }
    }
}