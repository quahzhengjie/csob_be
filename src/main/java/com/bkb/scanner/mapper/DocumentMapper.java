package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.DocumentDto;
import com.bkb.scanner.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "isCurrentForCase", target = "isCurrentForCase")
    DocumentDto toDto(Document entity);

    @Mapping(target = "content", ignore = true)
    @Mapping(source = "isCurrentForCase", target = "isCurrentForCase")
    Document toEntity(DocumentDto dto);
}