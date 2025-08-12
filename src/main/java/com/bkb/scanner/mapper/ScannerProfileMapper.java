package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.ScannerProfileDto;
import com.bkb.scanner.entity.ScannerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScannerProfileMapper {
    ScannerProfileDto toDto(ScannerProfile entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    ScannerProfile toEntity(ScannerProfileDto dto);
}