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
        newCase.setCaseId("CASE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newCase.setStatus(request.getStatus());
        newCase.setRiskLevel(request.getRiskLevel());
        newCase.setWorkflowStage("prospect");
        newCase.setSlaDeadline(Instant.now().plus(7, ChronoUnit.DAYS));

        CaseEntityData entityData = new CaseEntityData();
        entityData.setEntityName(request.getEntityName());
        entityData.setEntityType(request.getEntityType());
        entityData.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        newCase.setEntityData(entityData);

        Case savedCase = caseRepository.save(newCase);
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

        // ADD THIS LINE - Update workflow stage based on status
        c.setWorkflowStage(mapStatusToWorkflowStage(status));

        return caseMapper.toDto(caseRepository.save(c));
    }

    // ADD THIS HELPER METHOD
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
        return caseMapper.toDto(updatedCase);
    }
}