package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Instant;
import java.util.List;

/**
 * Entity representing a call report or interaction log
 * All timestamps are stored in UTC
 */
@Entity
@Table(name = "call_reports")
@Data
@EqualsAndHashCode(callSuper = false)
public class CallReport extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_date", nullable = false)
    private Instant callDate;  // UTC timestamp of when the call/meeting occurred

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(name = "next_steps", length = 1000)
    private String nextSteps;

    // Enhanced fields
    @Column(name = "call_type")
    private String callType; // 'Inbound', 'Outbound', 'Meeting', 'Email'

    @Column(name = "duration")
    private Integer duration; // Duration in minutes

    @Column(name = "outcome")
    private String outcome; // 'Positive', 'Neutral', 'Negative', 'Follow-up Required'

    @ElementCollection
    @CollectionTable(name = "call_report_attendees", joinColumns = @JoinColumn(name = "call_report_id"))
    @Column(name = "attendee_name")
    private List<String> attendees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case ownerCase;

    // Soft delete fields
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Column(name = "deleted_date")
    private Instant deletedDate;  // UTC timestamp of when it was deleted
}