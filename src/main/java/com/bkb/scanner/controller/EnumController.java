package com.bkb.scanner.controller;

import com.bkb.scanner.dto.EnumConfigDto;
import com.bkb.scanner.service.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enums")
public class EnumController {

    @Autowired
    private EnumService enumService;

    @GetMapping
    public ResponseEntity<EnumConfigDto> getEnums() {
        EnumConfigDto config = enumService.getEnumConfig();

        // Debug logging
        System.out.println("=== Returning enum config ===");
        config.getRoles().forEach((key, value) -> {
            System.out.println("Role: " + key + " -> " + value.getLabel());
            if (value.getPermissions() != null) {
                System.out.println("  Permissions (" + value.getPermissions().size() + "): " + value.getPermissions().keySet());
            } else {
                System.out.println("  Permissions: NULL");
            }
        });
        System.out.println("=== End enum config ===");

        return ResponseEntity.ok(config);
    }
}