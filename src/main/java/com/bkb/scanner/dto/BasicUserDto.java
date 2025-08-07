package com.bkb.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic user information DTO that can be accessed by any authenticated user.
 * Contains only non-sensitive information needed for display purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicUserDto {
    private String userId;
    private String name;
    private String department;
}