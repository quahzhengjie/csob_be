
// Cases Page DTO
package com.bkb.scanner.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class CasesPageDto {
    private List<CaseDto> data;
    private PaginationInfo pagination;
    private MetaInfo meta;

    @Data
    @Builder
    public static class PaginationInfo {
        private Integer page;
        private Integer limit;
        private Long total;
        private Integer totalPages;
    }

    @Data
    @Builder
    public static class MetaInfo {
        private AppliedFilters appliedFilters;
    }

    @Data
    @Builder
    public static class AppliedFilters {
        private List<String> riskLevel;
        private List<String> status;
    }
}