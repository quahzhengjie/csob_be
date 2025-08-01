package com.bkb.scanner.service;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.*;
import com.bkb.scanner.mapper.CaseMapper;
import com.bkb.scanner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        // ADD DEBUGGING HERE
        System.out.println("=== CREATE CASE DEBUG ===");
        System.out.println("Request received: " + request);

        // Handle the new entity details if provided
        if (request.getEntity() != null) {
            CaseCreationRequest.EntityDetails details = request.getEntity();

            System.out.println("Entity details found:");
            System.out.println("- BasicNumber: " + details.getBasicNumber());
            System.out.println("- BusinessActivity: " + details.getBusinessActivity());
            System.out.println("- ContactPerson: " + details.getContactPerson());
            System.out.println("- ContactEmail: " + details.getContactEmail());
            System.out.println("- ContactPhone: " + details.getContactPhone());

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
                System.out.println("SET BusinessActivity: " + entityData.getBusinessActivity());
            }
            if (details.getContactPerson() != null) {
                entityData.setContactPerson(details.getContactPerson());
                System.out.println("SET ContactPerson: " + entityData.getContactPerson());
            }
            if (details.getContactEmail() != null) {
                entityData.setContactEmail(details.getContactEmail());
                System.out.println("SET ContactEmail: " + entityData.getContactEmail());
            }
            if (details.getContactPhone() != null) {
                entityData.setContactPhone(details.getContactPhone());
                System.out.println("SET ContactPhone: " + entityData.getContactPhone());
            }
        } else {
            System.out.println("NO ENTITY DETAILS IN REQUEST!");
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

        System.out.println("Before save - EntityData:");
        System.out.println("- BusinessActivity: " + entityData.getBusinessActivity());
        System.out.println("- ContactPerson: " + entityData.getContactPerson());
        System.out.println("- ContactEmail: " + entityData.getContactEmail());
        System.out.println("- ContactPhone: " + entityData.getContactPhone());

        Case savedCase = caseRepository.save(newCase);

        System.out.println("After save - Saved case entity data:");
        System.out.println("- BusinessActivity: " + savedCase.getEntityData().getBusinessActivity());
        System.out.println("- ContactPerson: " + savedCase.getEntityData().getContactPerson());
        System.out.println("=== END DEBUG ===");

        // Log the activity
        ActivityLog log = new ActivityLog();
        log.setType("CASE_CREATED");
        log.setDetails("Case created with entity: " + request.getEntityName() + " (Type: " + request.getEntityType() + ")");
        log.setOwnerCase(savedCase);
        activityLogRepository.save(log);

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
        Case ownerCase = caseRepository.findById(caseId).orElseThrow(() -> new RuntimeException("Case not found"));
        ActivityLog log = caseMapper.toEntity(activityDto);
        log.setOwnerCase(ownerCase);
        ActivityLog savedLog = activityLogRepository.save(log);
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

        // Log the update activity
        ActivityLog log = new ActivityLog();
        log.setType("ENTITY_UPDATED");
        log.setDetails("Entity profile updated");
        log.setOwnerCase(updatedCase);
        activityLogRepository.save(log);

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

        // Log the activity
        ActivityLog log = new ActivityLog();
        log.setType("CALL_REPORT_UPDATED");
        log.setDetails("Updated call report ID: " + reportId);
        log.setOwnerCase(ownerCase);
        activityLogRepository.save(log);

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

        // Log the activity
        ActivityLog log = new ActivityLog();
        log.setType("CALL_REPORT_DELETED");
        log.setDetails(String.format("Soft deleted call report ID: %d. Reason: %s",
                reportId, deletionReason));
        log.setOwnerCase(ownerCase);
        activityLogRepository.save(log);
    }

    // Helper method to get current user - implement based on your security setup
    private String getCurrentUserId() {
        // With Spring Security:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // if (auth != null && auth.getPrincipal() instanceof UserDetails) {
        //     return ((UserDetails) auth.getPrincipal()).getUsername();
        // }
        return "USER-001"; // Placeholder - replace with actual implementation
    }
}