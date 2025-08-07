// In TemplateDocDto.java:

package com.bkb.scanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null fields from JSON output
public class TemplateDocDto {
    private String name;
    private boolean required;
    private String description;
    private Integer validityMonths;
    private String category; // ADD THIS FIELD
}