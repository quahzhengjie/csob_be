package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The complete mapper interface for converting between Case-related entities and DTOs.
 * It uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface CaseMapper {

    // --- Entity to DTO Mappings ---

    @Mapping(source = "entityData", target = "entity")
    @Mapping(source = "assignedTo.userId", target = "assignedTo")
    @Mapping(source = "approvedBy.userId", target = "approvedBy")
    @Mapping(source = "relatedParties", target = "relatedPartyLinks", qualifiedByName = "mapRelatedParties")
    @Mapping(source = "activityLogs", target = "activities")
    @Mapping(source = "callReports", target = "callReports")
    @Mapping(target = "approvalChain", expression = "java(mapApprovalChain(entity))")
    CaseDto toDto(Case entity);

    // Method to map approval chain - for now, return empty list
    default List<String> mapApprovalChain(Case entity) {
        // In a real implementation, this would extract the approval chain from somewhere
        // For now, returning an empty list to satisfy the frontend requirement
        List<String> approvalChain = new ArrayList<>();
        if (entity.getRiskLevel() != null && entity.getRiskLevel().equals("High")) {
            // High risk cases might need manager approval
            approvalChain.add("USER-002");
        }
        return approvalChain;
    }

    @Named("mapRelatedParties")
    default List<RelatedPartyLinkDto> mapRelatedParties(List<RelatedParty> relatedParties) {
        if (relatedParties == null) {
            return new ArrayList<>();
        }

        // Group related parties by partyId
        return relatedParties.stream()
                .collect(Collectors.groupingBy(rp -> rp.getParty().getPartyId()))
                .entrySet().stream()
                .map(entry -> {
                    RelatedPartyLinkDto link = new RelatedPartyLinkDto();
                    link.setPartyId(entry.getKey());

                    // Use var to avoid the complex type declaration
                    var relationships = entry.getValue().stream()
                            .map(rp -> {
                                var rel = new RelatedPartyLinkDto.RelationshipDto();
                                rel.setType(rp.getRelationshipType());
                                rel.setOwnershipPercentage(rp.getOwnershipPercentage());
                                return rel;
                            })
                            .collect(Collectors.toList());

                    link.setRelationships(relationships);
                    return link;
                })
                .collect(Collectors.toList());
    }

    EntityDataDto toDto(CaseEntityData entityData);

    DocumentDto toDto(Document entity);

    @Mapping(source = "party.partyId", target = "partyId")
    @Mapping(source = "party.name", target = "name")
    @Mapping(source = "relationshipType", target = "relationshipType")
    @Mapping(source = "ownershipPercentage", target = "ownershipPercentage")
    RelatedPartyDto toDto(RelatedParty entity);

    // Now field names match, so explicit mappings for email/phone/address are optional
    // but let's keep them for clarity
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    PartyDto toDto(Party entity);

    @Mapping(source = "callDate", target = "callDate")
    @Mapping(source = "summary", target = "summary")
    @Mapping(source = "nextSteps", target = "nextSteps")
    @Mapping(target = "reportId", expression = "java(formatReportId(entity.getId()))")
    CallReportDto toDto(CallReport entity);

    default String formatReportId(Long id) {
        return id != null ? "CR-" + String.format("%03d", id) : null;
    }

    @Mapping(source = "type", target = "type")
    @Mapping(source = "details", target = "details")
    @Mapping(source = "createdBy", target = "performedBy")
    @Mapping(source = "createdDate", target = "timestamp")
    @Mapping(target = "activityId", expression = "java(formatActivityId(entity.getId()))")
    ActivityLogDto toDto(ActivityLog entity);

    default String formatActivityId(Long id) {
        return id != null ? "ACT-" + String.format("%03d", id) : null;
    }

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleNames")
    UserDto toDto(User entity);

    @Named("rolesToRoleNames")
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    // --- DTO to Entity Mappings ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerCase", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    RelatedParty toEntity(RelatedPartyDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerCase", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    CallReport toEntity(CallReportDto dto);

    // To this (map performedBy to createdBy):
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerCase", ignore = true)
    @Mapping(source = "performedBy", target = "createdBy")  // <-- MAP performedBy to createdBy!
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    ActivityLog toEntity(ActivityLogDto dto);

    @Mapping(target = "partyId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Party toEntity(PartyCreationRequest dto);

    // Explicit mappings for email/phone/address to ensure they're included
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(target = "partyId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "documents", ignore = true)
    void updateEntityFromDto(PartyUpdateRequest dto, @MappingTarget Party entity);
}