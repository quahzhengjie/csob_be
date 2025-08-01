package com.bkb.scanner.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EnumConfigDto {
    private Map<String, RoleDto> roles;
    private Map<String, List<String>> enums;
}