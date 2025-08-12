package com.bkb.scanner.controller;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.service.PartyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing parties (individuals) independently of cases.
 * This allows for a master list of individuals that can be linked to multiple cases.
 */
@RestController
@RequestMapping("/parties")
public class PartyController {

    @Autowired
    private PartyService partyService;

    @GetMapping
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<PartyDto>> getAllParties() {
        return ResponseEntity.ok(partyService.getAllParties());
    }

    @GetMapping("/{partyId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<PartyDto> getPartyById(@PathVariable String partyId) {
        return partyService.getPartyById(partyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<PartyDto> createParty(@Valid @RequestBody PartyCreationRequest request) {
        return new ResponseEntity<>(partyService.createParty(request), HttpStatus.CREATED);
    }

    @PutMapping("/{partyId}")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<PartyDto> updateParty(
            @PathVariable String partyId,
            @Valid @RequestBody PartyUpdateRequest request) {
        return ResponseEntity.ok(partyService.updateParty(partyId, request));
    }

    @DeleteMapping("/{partyId}")
    @PreAuthorize("hasAuthority('case:update')")
    public ResponseEntity<Void> deleteParty(@PathVariable String partyId) {
        partyService.deleteParty(partyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{partyId}/documents")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<PartyWithDocumentsDto> getPartyWithDocuments(@PathVariable String partyId) {
        return partyService.getPartyWithDocuments(partyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<PartyDto>> searchParties(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(partyService.searchParties(query));
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAuthority('case:read')")
    public ResponseEntity<List<PartyDto>> getPartiesByCase(@PathVariable String caseId) {
        return ResponseEntity.ok(partyService.getPartiesByCaseId(caseId));
    }
}