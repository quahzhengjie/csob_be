package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entity representing a customer onboarding case
 */
@Entity
@Table(name = "csob_cases")
@Data
@EqualsAndHashCode(callSuper = false)
public class Case extends Auditable {
    @Id
    @Column(name = "case_id")
    private String caseId;

    @Column(nullable = false)
    private String status;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "workflow_stage")
    private String workflowStage;

    @Column(name = "sla_deadline")
    private Instant slaDeadline;

    @Embedded
    private CaseEntityData entityData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @OneToMany(mappedBy = "ownerCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatedParty> relatedParties = new ArrayList<>();

    @OneToMany(mappedBy = "ownerCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CallReport> callReports = new ArrayList<>();

    @OneToMany(mappedBy = "ownerCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityLog> activityLogs = new ArrayList<>();

    // REMOVED THE DUPLICATE FIELDS - they are already in CaseEntityData
    // The fields contactPerson, contactEmail, contactPhone, and businessActivity
    // should ONLY be in CaseEntityData, not here

    // Helper methods
    public void addRelatedParty(RelatedParty party) {
        relatedParties.add(party);
        party.setOwnerCase(this);
    }

    public void removeRelatedParty(RelatedParty party) {
        relatedParties.remove(party);
        party.setOwnerCase(null);
    }

    public void addCallReport(CallReport report) {
        callReports.add(report);
        report.setOwnerCase(this);
    }

    public void addActivityLog(ActivityLog log) {
        activityLogs.add(log);
        log.setOwnerCase(this);
    }
}