package com.bkb.scanner.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EnumConfigDto {

    private Map<String, RoleDto> roles;

    private Map<String, List<String>> enums;

    // ** ADD THIS FIELD **
    // This is the missing piece. It adds the new structured object
    // to the configuration that gets sent to the frontend.
    private DocumentRequirementsDto documentRequirements;
}