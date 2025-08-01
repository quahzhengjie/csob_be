// FILE: src/main/java/com/bkb/scanner/config/DocumentRequirements.java
package com.bkb.scanner.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Represents the entire "Rulebook" of document requirements for the application,
 * deserialized from the JSON configuration stored in the database.
 */
@Data
public class DocumentRequirements {
    private Map<String, List<TemplateDoc>> individualTemplates;
    private Map<String, List<TemplateDoc>> entityTemplates;
    private BankFormTemplates bankFormTemplates;
    private Map<String, List<TemplateDoc>> riskBasedDocuments;
    private Map<String, List<String>> entityRoleMapping;

    @Data
    public static class BankFormTemplates {
        private List<String> corporate;
        private List<String> individualStakeholder;
    }
}