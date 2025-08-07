package com.bkb.scanner.service;

import com.bkb.scanner.dto.ScannerProfileDto;
import com.bkb.scanner.entity.ScannerProfile;
import com.bkb.scanner.mapper.ScannerProfileMapper;
import com.bkb.scanner.repository.ScannerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScannerProfileService {

    @Autowired private ScannerProfileRepository profileRepository;
    @Autowired private ScannerProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public List<ScannerProfileDto> findAll() {
        return profileRepository.findAll().stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScannerProfileDto create(ScannerProfileDto dto) {
        ScannerProfile profile = profileMapper.toEntity(dto);
        ScannerProfile savedProfile = profileRepository.save(profile);
        return profileMapper.toDto(savedProfile);
    }

    @Transactional
    public void delete(Long id) {
        profileRepository.deleteById(id);
    }
}