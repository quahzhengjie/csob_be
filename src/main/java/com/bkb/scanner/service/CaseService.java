package com.bkb.scanner.service;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.*;
import com.bkb.scanner.mapper.CaseMapper;
import com.bkb.scanner.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The fully implemented service layer for handling all business logic related to Cases.
 */
@Service
public class CaseService {
    @Autowired private CaseRepository caseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RelatedPartyRepository relatedPartyRepository;
    @Autowired private CallReportRepository callReportRepository;
    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private CaseMapper caseMapper;
    @Autowired private PartyRepository partyRepository;
    @Autowired private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<CaseDto> getAllCases() {
        return caseRepository.findAll().stream()
                .map(caseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CaseDto> getCaseById(String caseId) {
        return caseRepository.findById(caseId)
                .map(caseMapper::toDto);
    }

    @Transactional
    public CaseDto createCase(CaseCreationRequest request) {
        Case newCase = new Case();

        // Generate meaningful case ID: CASE-YYYYMM-XXXX (e.g., CASE-202501-0042)
        String yearMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        long caseCount = caseRepository.countByCreatedDateBetween(
                java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                java.time.LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        ) + 1;
        String caseId = String.format("CASE-%s-%04d", yearMonth, caseCount);
        newCase.setCaseId(caseId);

        newCase.setStatus(request.getStatus());
        newCase.setRiskLevel(request.getRiskLevel());
        newCase.setWorkflowStage("prospect");
        newCase.setSlaDeadline(Instant.now().plus(7, ChronoUnit.DAYS));

        CaseEntityData entityData = new CaseEntityData();
        entityData.setEntityName(request.getEntityName());
        entityData.setEntityType(request.getEntityType());

        // Set customerId same as caseId - maintains backward compatibility
        entityData.setCustomerId(caseId);

        // Handle the new entity details if provided
        if (request.getEntity() != null) {
            CaseCreationRequest.EntityDetails details = request.getEntity();

            // Required field
            entityData.setBasicNumber(details.getBasicNumber());

            // Optional fields - only set if provided
            if (details.getCisNumber() != null) {
                entityData.setCisNumber(details.getCisNumber());
            }
            if (details.getTaxId() != null) {
                entityData.setTaxId(details.getTaxId());
            }
            if (details.getAddress1() != null) {
                entityData.setAddress1(details.getAddress1());
            }
            if (details.getAddress2() != null) {
                entityData.setAddress2(details.getAddress2());
            }
            if (details.getAddressCountry() != null) {
                entityData.setAddressCountry(details.getAddressCountry());
            }
            if (details.getPlaceOfIncorporation() != null) {
                entityData.setPlaceOfIncorporation(details.getPlaceOfIncorporation());
            }
            // NEW FIELDS in creation
            if (details.getBusinessActivity() != null) {
                entityData.setBusinessActivity(details.getBusinessActivity());
            }
            if (details.getContactPerson() != null) {
                entityData.setContactPerson(details.getContactPerson());
            }
            if (details.getContactEmail() != null) {
                entityData.setContactEmail(details.getContactEmail());
            }
            if (details.getContactPhone() != null) {
                entityData.setContactPhone(details.getContactPhone());
            }
        }

        // Set default values for any required fields not in the request
        if (entityData.getAddressCountry() == null) {
            entityData.setAddressCountry("Singapore");
        }
        if (entityData.getPlaceOfIncorporation() == null) {
            entityData.setPlaceOfIncorporation("Singapore");
        }
        if (entityData.getUsFatcaClassificationFinal() == null) {
            entityData.setUsFatcaClassificationFinal("Non-US Entity");
        }

        newCase.setEntityData(entityData);

        Case savedCase = caseRepository.save(newCase);

        // Log the activity with proper user attribution
        ActivityLog log = new ActivityLog();
        log.setType("CASE_CREATED");
        log.setDetails("Case created with entity: " + request.getEntityName() + " (Type: " + request.getEntityType() + ")");
        log.setOwnerCase(savedCase);

        String currentUserId = getCurrentUserId();
        System.out.println("🔴 [CASE CREATE] Setting createdBy to: " + currentUserId);
        log.setCreatedBy(currentUserId);

        ActivityLog savedLog = activityLogRepository.save(log);
        System.out.println("🔴 [CASE CREATE] Saved log with createdBy: " + savedLog.getCreatedBy());

        return caseMapper.toDto(savedCase);
    }

    @Transactional
    public RelatedPartyDto addRelatedParty(String caseId, RelatedPartyDto partyDto) {
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Look up the Party entity
        Party party = partyRepository.findById(partyDto.getPartyId())
                .orElseThrow(() -> new RuntimeException("Party not found: " + partyDto.getPartyId()));

        // Create the RelatedParty entity
        RelatedParty relatedParty = new RelatedParty();
        relatedParty.setParty(party); // Set the Party entity reference
        relatedParty.setRelationshipType(partyDto.getRelationshipType());
        relatedParty.setOwnershipPercentage(partyDto.getOwnershipPercentage());
        relatedParty.setOwnerCase(ownerCase);

        RelatedParty savedParty = relatedPartyRepository.save(relatedParty);
        return caseMapper.toDto(savedParty);
    }

    @Transactional
    public CallReportDto addCallReport(String caseId, CallReportDto reportDto) {
        Case ownerCase = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        CallReport report = caseMapper.toEntity(reportDto);
        report.setOwnerCase(ownerCase);
        CallReport savedReport = callReportRepository.save(report);
        return caseMapper.toDto(savedReport);
    }

    @Transactional
    public ActivityLogDto addActivityLog(String caseId, ActivityLogDto activityDto) {
        System.out.println("🔵 === ACTIVITY LOG SERVICE METHOD CALLED ===");
        System.out.println("🔵 CaseId: " + caseId);
        System.out.println("🔵 ActivityDto type: " + activityDto.getType());
        System.out.println("🔵 ActivityDto details: " + activityDto.getDetails());
        System.out.println("🔵 ActivityDto performedBy: " + activityDto.getPerformedBy());

        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Map DTO to entity - the mapper should map performedBy to createdBy
        ActivityLog log = caseMapper.toEntity(activityDto);
        log.setOwnerCase(ownerCase);

        System.out.println("🔵 After mapping - Entity createdBy: " + log.getCreatedBy());

        // Force set the createdBy if it's still null or wrong
        if (activityDto.getPerformedBy() != null && !activityDto.getPerformedBy().isEmpty()) {
            System.out.println("🔵 Manually setting createdBy to: " + activityDto.getPerformedBy());
            log.setCreatedBy(activityDto.getPerformedBy());
        }

        System.out.println("🔵 Before save - Entity createdBy: " + log.getCreatedBy());

        // Save the log
        ActivityLog savedLog = activityLogRepository.save(log);

        System.out.println("🔵 After save - Saved createdBy: " + savedLog.getCreatedBy());

        // If JPA Auditing overrode it, force update with native query
        if (activityDto.getPerformedBy() != null &&
                !activityDto.getPerformedBy().isEmpty() &&
                !activityDto.getPerformedBy().equals(savedLog.getCreatedBy())) {

            System.out.println("🔵 JPA Auditing overrode! Forcing update...");
            System.out.println("🔵 Expected: " + activityDto.getPerformedBy());
            System.out.println("🔵 Got: " + savedLog.getCreatedBy());

            // Force update with native query
            int updated = entityManager.createNativeQuery(
                            "UPDATE csob_activity_logs SET created_by = :performedBy WHERE id = :id")
                    .setParameter("performedBy", activityDto.getPerformedBy())
                    .setParameter("id", savedLog.getId())
                    .executeUpdate();

            System.out.println("🔵 Native query updated " + updated + " rows");

            // Refresh the entity
            entityManager.refresh(savedLog);
            System.out.println("🔵 After refresh - Final createdBy: " + savedLog.getCreatedBy());
        }

        System.out.println("🔵 === END ACTIVITY LOG SERVICE ===");

        return caseMapper.toDto(savedLog);
    }

    @Transactional
    public CaseDto assignCase(String caseId, String userId) {
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        c.setAssignedTo(user);
        return caseMapper.toDto(caseRepository.save(c));
    }

    @Transactional
    public void removeRelatedParty(String caseId, String partyId, String relationshipType) {
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Find and remove the specific relationship
        ownerCase.getRelatedParties().removeIf(rp ->
                rp.getParty().getPartyId().equals(partyId) &&
                        rp.getRelationshipType().equals(relationshipType)
        );

        caseRepository.save(ownerCase);
    }

    @Transactional
    public CaseDto updateCaseStatus(String caseId, String status, String riskLevel) {
        Case c = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        c.setStatus(status);
        c.setRiskLevel(riskLevel);

        // Update workflow stage based on status
        c.setWorkflowStage(mapStatusToWorkflowStage(status));

        return caseMapper.toDto(caseRepository.save(c));
    }

    // Helper method to map status to workflow stage
    private String mapStatusToWorkflowStage(String status) {
        switch (status) {
            case "Prospect":
                return "prospect";
            case "KYC Review":
                return "kyc_review";
            case "Pending Approval":
                return "approval";
            case "Active":
            case "Rejected":
                return "completed";
            default:
                return "prospect";
        }
    }

    @Transactional
    public CaseDto updateEntityData(String caseId, EntityDataDto entityDataDto) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        CaseEntityData entityData = caseEntity.getEntityData();
        entityData.setEntityName(entityDataDto.getEntityName());
        entityData.setEntityType(entityDataDto.getEntityType());
        entityData.setBasicNumber(entityDataDto.getBasicNumber());
        entityData.setCisNumber(entityDataDto.getCisNumber());
        entityData.setTaxId(entityDataDto.getTaxId());
        entityData.setAddress1(entityDataDto.getAddress1());
        entityData.setAddress2(entityDataDto.getAddress2());
        entityData.setAddressCountry(entityDataDto.getAddressCountry());
        entityData.setPlaceOfIncorporation(entityDataDto.getPlaceOfIncorporation());
        entityData.setUsFatcaClassificationFinal(entityDataDto.getUsFatcaClassificationFinal());

        // NEW FIELDS - Update the additional contact and business fields
        entityData.setBusinessActivity(entityDataDto.getBusinessActivity());
        entityData.setContactPerson(entityDataDto.getContactPerson());
        entityData.setContactEmail(entityDataDto.getContactEmail());
        entityData.setContactPhone(entityDataDto.getContactPhone());

        // Update credit details if provided
        if (entityDataDto.getCreditDetails() != null) {
            CreditDetails creditDetails = entityData.getCreditDetails();
            if (creditDetails == null) {
                creditDetails = new CreditDetails();
                entityData.setCreditDetails(creditDetails);
            }
            creditDetails.setCreditLimit(entityDataDto.getCreditDetails().getCreditLimit());
            creditDetails.setCreditScore(entityDataDto.getCreditDetails().getCreditScore());
            creditDetails.setAssessmentNotes(entityDataDto.getCreditDetails().getAssessmentNotes());
        }

        Case updatedCase = caseRepository.save(caseEntity);

        // Log the update activity with proper user attribution
        ActivityLog log = new ActivityLog();
        log.setType("ENTITY_UPDATED");
        log.setDetails("Entity profile updated");
        log.setOwnerCase(updatedCase);

        String currentUserId = getCurrentUserId();
        System.out.println("🔴 [ENTITY UPDATE] Setting createdBy to: " + currentUserId);
        log.setCreatedBy(currentUserId);

        ActivityLog savedLog = activityLogRepository.save(log);
        System.out.println("🔴 [ENTITY UPDATE] Saved log with createdBy: " + savedLog.getCreatedBy());

        return caseMapper.toDto(updatedCase);
    }

    @Transactional
    public CallReportDto updateCallReport(String caseId, Long reportId, CallReportDto reportDto) {
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        CallReport report = callReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Call report not found"));

        // Verify the report belongs to this case and is not deleted
        if (!report.getOwnerCase().getCaseId().equals(caseId)) {
            throw new RuntimeException("Call report does not belong to this case");
        }

        if (report.isDeleted()) {
            throw new RuntimeException("Cannot update a deleted call report");
        }

        // Update fields
        report.setCallDate(reportDto.getCallDate());
        report.setSummary(reportDto.getSummary());
        report.setNextSteps(reportDto.getNextSteps());
        report.setCallType(reportDto.getCallType());
        report.setDuration(reportDto.getDuration());
        report.setOutcome(reportDto.getOutcome());
        report.setAttendees(reportDto.getAttendees());

        CallReport updatedReport = callReportRepository.save(report);

        // Log the activity with proper user attribution
        ActivityLog log = new ActivityLog();
        log.setType("CALL_REPORT_UPDATED");
        log.setDetails("Updated call report ID: " + reportId);
        log.setOwnerCase(ownerCase);

        String currentUserId = getCurrentUserId();
        System.out.println("🔴 [CALL REPORT UPDATE] Setting createdBy to: " + currentUserId);
        log.setCreatedBy(currentUserId);

        ActivityLog savedLog = activityLogRepository.save(log);
        System.out.println("🔴 [CALL REPORT UPDATE] Saved log with createdBy: " + savedLog.getCreatedBy());

        return caseMapper.toDto(updatedReport);
    }

    @Transactional
    public void deleteCallReport(String caseId, Long reportId, String deletionReason) {
        Case ownerCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        CallReport report = callReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Call report not found"));

        // Verify the report belongs to this case
        if (!report.getOwnerCase().getCaseId().equals(caseId)) {
            throw new RuntimeException("Call report does not belong to this case");
        }

        if (report.isDeleted()) {
            throw new RuntimeException("Call report is already deleted");
        }

        // Soft delete the report
        report.setDeleted(true);
        report.setDeletionReason(deletionReason);
        report.setDeletedBy(getCurrentUserId());
        report.setDeletedDate(Instant.now());

        callReportRepository.save(report);

        // Log the activity with proper user attribution
        ActivityLog log = new ActivityLog();
        log.setType("CALL_REPORT_DELETED");
        log.setDetails(String.format("Soft deleted call report ID: %d. Reason: %s",
                reportId, deletionReason));
        log.setOwnerCase(ownerCase);

        String currentUserId = getCurrentUserId();
        System.out.println("🔴 [CALL REPORT DELETE] Setting createdBy to: " + currentUserId);
        log.setCreatedBy(currentUserId);

        ActivityLog savedLog = activityLogRepository.save(log);
        System.out.println("🔴 [CALL REPORT DELETE] Saved log with createdBy: " + savedLog.getCreatedBy());
    }

    /**
     * Get current user from Spring Security context (same pattern as DocumentService)
     */
    private User getCurrentUser() {
        System.out.println("🟡 === getCurrentUser() CALLED ===");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            System.out.println("🟡 Authentication is NULL");
            return null;
        }

        System.out.println("🟡 Authentication exists: " + authentication.getName());
        System.out.println("🟡 Is authenticated: " + authentication.isAuthenticated());
        System.out.println("🟡 Principal type: " + authentication.getPrincipal().getClass().getName());

        if (authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            System.out.println("🟡 Looking up user by username: " + username);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("🟡 Found user: " + user.getUserId() + " (" + user.getName() + ")");
                return user;
            } else {
                System.out.println("🟡 User not found in database for username: " + username);
                throw new RuntimeException("User not found: " + username);
            }
        }

        System.out.println("🟡 Returning null (system user)");
        return null;
    }

    /**
     * Helper method to get current user ID
     */
    private String getCurrentUserId() {
        System.out.println("🟢 === getCurrentUserId() CALLED ===");
        User currentUser = getCurrentUser();
        String userId = currentUser != null ? currentUser.getUserId() : "SYSTEM";
        System.out.println("🟢 Returning userId: " + userId);
        return userId;
    }

    // =====================================================================
    // REST OF THE METHODS (pagination, etc.) - unchanged
    // =====================================================================

    @Transactional(readOnly = true)
    public CasesPageDto getCasesWithFilters(
            Integer page,
            Integer limit,
            String search,
            String riskLevelFilter,
            String statusFilter,
            String sortBy,
            String sortOrder) {

        // Parse filters
        List<String> riskLevels = parseFilter(riskLevelFilter);
        List<String> statuses = parseFilter(statusFilter);

        // Create pageable with sorting
        Pageable pageable = createPageable(page - 1, limit, sortBy, sortOrder);

        // Execute query with filters - using boolean flags for null checks
        Page<Case> casesPage = caseRepository.findCasesWithFilters(
                search,
                !riskLevels.isEmpty(),
                riskLevels,
                !statuses.isEmpty(),
                statuses,
                pageable
        );

        // Convert to DTOs
        List<CaseDto> caseDtos = casesPage.getContent().stream()
                .map(caseMapper::toDto)
                .collect(Collectors.toList());

        // Build response
        return CasesPageDto.builder()
                .data(caseDtos)
                .pagination(CasesPageDto.PaginationInfo.builder()
                        .page(page)
                        .limit(limit)
                        .total(casesPage.getTotalElements())
                        .totalPages(casesPage.getTotalPages())
                        .build())
                .meta(CasesPageDto.MetaInfo.builder()
                        .appliedFilters(CasesPageDto.AppliedFilters.builder()
                                .riskLevel(riskLevels)
                                .status(statuses)
                                .build())
                        .build())
                .build();
    }

    private List<String> parseFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(filter.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Pageable createPageable(int page, int limit, String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String sortField = mapSortField(sortBy);
        Sort sort = Sort.by(direction, sortField);

        return PageRequest.of(page, limit, sort);
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null) {
            return "createdDate";
        }

        switch (sortBy) {
            case "entityName":
                return "entityData.entityName";
            case "caseId":
                return "caseId";
            case "riskLevel":
                return "riskLevel";
            case "status":
                return "status";
            case "createdDate":
                return "createdDate";
            default:
                return "createdDate";
        }
    }
}