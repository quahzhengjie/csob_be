package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.PartyDto;
import com.bkb.scanner.entity.Party;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartyMapper {
    PartyDto toDto(Party entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Party toEntity(PartyDto dto);
}