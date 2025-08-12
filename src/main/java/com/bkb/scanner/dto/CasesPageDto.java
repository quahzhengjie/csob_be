package com.bkb.scanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated case search results
 * Can contain either CaseDto or CaseSummaryDto objects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasesPageDto {

    // This can be either List<CaseDto> or List<CaseSummaryDto>
    // CaseSummaryDto extends CaseDto so it's type-safe
    private List<? extends CaseDto> data;

    private PaginationInfo pagination;
    private MetaInfo meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer page;
        private Integer limit;
        private Long total;
        private Integer totalPages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaInfo {
        private AppliedFilters appliedFilters;
        // Optional metadata fields for enhanced functionality
        private Long queryTime; // Milliseconds taken for the query
        private Boolean includesDocumentSummary; // Flag to indicate if summary data is included
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedFilters {
        private List<String> riskLevel;
        private List<String> status;
        // Optional: Add search term if you want to track it
        // private String searchTerm;
    }
}