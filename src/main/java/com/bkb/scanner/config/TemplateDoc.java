// FILE: src/main/java/com/bkb/scanner/config/TemplateDoc.java
package com.bkb.scanner.config;

import lombok.Data;

/**
 * Represents a single document requirement rule within the application's configuration.
 */
@Data
public class TemplateDoc {
    private String name;
    private boolean required;
    private Integer validityMonths;
    private String description;
    private String note;
}