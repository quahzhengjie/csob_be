package com.bkb.scanner.service;

import com.bkb.scanner.dto.*;
import com.bkb.scanner.entity.Party;
import com.bkb.scanner.mapper.CaseMapper;
import com.bkb.scanner.repository.PartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartyService {
    @Autowired private PartyRepository partyRepository;
    @Autowired private CaseMapper caseMapper;

    @Transactional(readOnly = true)
    public List<PartyDto> getAllParties() {
        return partyRepository.findAll().stream()
                .map(caseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PartyDto> getPartyById(String partyId) {
        return partyRepository.findById(partyId).map(caseMapper::toDto);
    }

    @Transactional
    public PartyDto createParty(PartyCreationRequest request) {
        Party party = caseMapper.toEntity(request);
        party.setPartyId("PARTY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Party savedParty = partyRepository.save(party);
        return caseMapper.toDto(savedParty);
    }

    @Transactional
    public PartyDto updateParty(String partyId, PartyUpdateRequest request) {
        System.out.println("=== PartyService.updateParty ===");
        System.out.println("Request email: " + request.getEmail());
        System.out.println("Request phone: " + request.getPhone());
        System.out.println("Request address: " + request.getAddress());

        Party existingParty = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        System.out.println("Before mapping - Party email: " + existingParty.getEmail());
        System.out.println("Before mapping - Party phone: " + existingParty.getPhone());
        System.out.println("Before mapping - Party address: " + existingParty.getAddress());

        caseMapper.updateEntityFromDto(request, existingParty);

        System.out.println("After mapping - Party email: " + existingParty.getEmail());
        System.out.println("After mapping - Party phone: " + existingParty.getPhone());
        System.out.println("After mapping - Party address: " + existingParty.getAddress());

        Party updatedParty = partyRepository.save(existingParty);

        System.out.println("After save - Party email: " + updatedParty.getEmail());
        System.out.println("After save - Party phone: " + updatedParty.getPhone());
        System.out.println("After save - Party address: " + updatedParty.getAddress());

        PartyDto dto = caseMapper.toDto(updatedParty);
        System.out.println("DTO email: " + dto.getEmail());
        System.out.println("DTO phone: " + dto.getPhone());
        System.out.println("DTO address: " + dto.getAddress());

        return dto;
    }

    @Transactional
    public void deleteParty(String partyId) {
        if (!partyRepository.existsById(partyId)) {
            throw new RuntimeException("Party not found with id: " + partyId);
        }
        partyRepository.deleteById(partyId);
    }

    @Transactional(readOnly = true)
    public Optional<PartyWithDocumentsDto> getPartyWithDocuments(String partyId) {
        return partyRepository.findById(partyId).map(party -> {
            PartyWithDocumentsDto dto = new PartyWithDocumentsDto();
            dto.setParty(caseMapper.toDto(party));
            // dto.setDocuments(party.getDocuments().stream().map(caseMapper::toDto).collect(Collectors.toList()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public List<PartyDto> searchParties(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        return partyRepository.findByNameContainingIgnoreCaseOrPartyIdContainingIgnoreCase(query, query)
                .stream()
                .map(caseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartyDto> getPartiesByCaseId(String caseId) {
        return partyRepository.findPartiesByCaseId(caseId)
                .stream()
                .map(caseMapper::toDto)
                .collect(Collectors.toList());
    }
}