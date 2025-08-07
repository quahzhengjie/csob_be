package com.bkb.scanner.service;

import com.bkb.scanner.dto.DocumentRequirementsDto;
import com.bkb.scanner.dto.EnumConfigDto;
import com.bkb.scanner.dto.RoleDto;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.mapper.RoleMapper;
import com.bkb.scanner.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnumService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public EnumConfigDto getEnumConfig() {
        EnumConfigDto config = new EnumConfigDto();

        // 1. Get all user roles
        List<Role> roles = roleRepository.findAll();
        Map<String, RoleDto> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getName, roleMapper::toDto));
        config.setRoles(roleMap);

        // 4. Define the enums with the dynamic list
        Map<String, List<String>> enums = Map.of(
                "caseStatus", List.of("Prospect", "KYC Review", "Pending Approval", "Active", "Rejected"),
                "riskLevel", List.of("Low", "Medium", "High"),
                "docStatus", List.of("Missing", "Submitted", "Verified", "Rejected", "Expired")
        );
        config.setEnums(enums);

        return config;
    }
}