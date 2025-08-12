package com.bkb.scanner.mapper;

import com.bkb.scanner.dto.DocumentDto;
import com.bkb.scanner.entity.Document;
import com.bkb.scanner.entity.User;
import org.mapstruct.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface DocumentMapper {

    @Mapping(source = "isCurrentForCase", target = "isCurrentForCase")
    @Mapping(source = "isAdHoc", target = "isAdHoc", defaultValue = "false")
    @Mapping(target = "uploadedBy", expression = "java(mapUserToDto(entity.getUploadedByUser()))")
    @Mapping(target = "verifiedBy", expression = "java(mapUserToDto(entity.getVerifiedByUser()))")
    @Mapping(target = "uploadedDate", expression = "java(formatInstant(entity.getCreatedDate()))")
    @Mapping(target = "verifiedDate", expression = "java(formatInstant(entity.getVerifiedDate()))")
    @Mapping(target = "expiryDate", expression = "java(formatInstant(entity.getExpiryDate()))")
    DocumentDto toDto(Document entity);

    @Mapping(target = "content", ignore = true)
    @Mapping(target = "uploadedByUser", ignore = true)
    @Mapping(target = "verifiedByUser", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(source = "isCurrentForCase", target = "isCurrentForCase", defaultValue = "false")
    @Mapping(source = "isAdHoc", target = "isAdHoc", defaultValue = "false")
    Document toEntity(DocumentDto dto);

    default DocumentDto.UserInfoDto mapUserToDto(User user) {
        if (user == null) {
            return null;
        }
        return DocumentDto.UserInfoDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .department(user.getDepartment())
                .build();
    }

    default String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}