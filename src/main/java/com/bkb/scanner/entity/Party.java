package com.bkb.scanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "csob_parties")
@Data
@EqualsAndHashCode(callSuper = false)
public class Party extends Auditable {
    @Id
    @Column(name = "party_id")
    private String partyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "residency_status")
    private String residencyStatus;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "identity_no", unique = true)
    private String identityNo;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "is_pep")
    private boolean isPEP;

    @Column(name = "pep_country")
    private String pepCountry;

    @OneToMany(mappedBy = "ownerParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    // Add these fields to Party.java
    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", length = 500)
    private String address;
}