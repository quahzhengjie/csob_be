// FILE: src/main/java/com/bkb/scanner/repository/PartyRepository.java
package com.bkb.scanner.repository;

import com.bkb.scanner.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByNameContainingIgnoreCaseOrPartyIdContainingIgnoreCase(String name, String partyId);

    @Query("SELECT rp.party FROM RelatedParty rp WHERE rp.ownerCase.caseId = :caseId")
    List<Party> findPartiesByCaseId(@Param("caseId") String caseId);
}
