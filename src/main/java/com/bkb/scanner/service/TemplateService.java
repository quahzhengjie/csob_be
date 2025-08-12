package com.bkb.scanner.service;

import com.bkb.scanner.dto.DocumentRequirementsDto;
import com.bkb.scanner.dto.TemplateDocDto;
import com.bkb.scanner.dto.UpdateTemplateRequestDto;
import com.bkb.scanner.entity.DocumentRequirement;
import com.bkb.scanner.entity.EntityRole;
import com.bkb.scanner.entity.TemplateCategory;
import com.bkb.scanner.entity.TemplateType;
import com.bkb.scanner.repository.DocumentRequirementRepository;
import com.bkb.scanner.repository.EntityRoleRepository;
import com.bkb.scanner.repository.TemplateCategoryRepository;
import com.bkb.scanner.repository.TemplateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateCategoryRepository templateCategoryRepository;
    private final TemplateTypeRepository templateTypeRepository;
    private final EntityRoleRepository entityRoleRepository;
    private final DocumentRequirementRepository documentRequirementRepository;

    @Transactional(readOnly = true)
    public DocumentRequirementsDto getDocumentRequirements() {
        DocumentRequirementsDto dto = new DocumentRequirementsDto();

        // 1. Handle Document Requirement Templates
        List<TemplateCategory> categories = templateCategoryRepository.findAll();
        for (TemplateCategory category : categories) {
            Map<String, List<TemplateDocDto>> typesMap = category.getTypes().stream()
                    .filter(type -> !type.isDeleted())
                    .collect(Collectors.toMap(
                            TemplateType::getTypeKey,
                            this::convertTemplateTypeToDocDtos,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            switch (category.getCategoryKey()) {
                case "entityTemplates":
                    dto.setEntityTemplates(typesMap);
                    break;
                case "individualTemplates":
                    dto.setIndividualTemplates(typesMap);
                    break;
                case "riskBasedDocuments":
                    dto.setRiskBasedDocuments(typesMap);
                    break;
            }
        }

        // 2. Handle Entity Roles
        List<EntityRole> entityRoles = entityRoleRepository.findAll();
        Map<String, List<String>> entityRoleMap = entityRoles.stream()
                .sorted(Comparator.comparing(EntityRole::getDisplayOrder))
                .collect(Collectors.groupingBy(
                        EntityRole::getEntityType,
                        LinkedHashMap::new,
                        Collectors.mapping(EntityRole::getRoleName, Collectors.toList())
                ));
        dto.setEntityRoleMapping(entityRoleMap);

        return dto;
    }

    private List<TemplateDocDto> convertTemplateTypeToDocDtos(TemplateType type) {
        return type.getDocumentRequirements().stream()
                .filter(req -> !req.isDeleted())
                .sorted(Comparator.comparing(DocumentRequirement::getDisplayOrder))
                .map(req -> {
                    TemplateDocDto docDto = new TemplateDocDto();
                    docDto.setName(req.getName());
                    docDto.setDescription(req.getDescription());
                    docDto.setRequired(req.isRequired());
                    docDto.setValidityMonths(req.getValidityMonths());
                    docDto.setCategory(req.getCategory()); // <-- ADD THIS LINE
                    return docDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentRequirementsDto updateDocumentRequirements(UpdateTemplateRequestDto request) {
        TemplateType templateType = templateTypeRepository.findByTypeKeyAndCategory_CategoryKey(
                        request.getTypeKey(), request.getCategoryKey())
                .orElseThrow(() -> new RuntimeException("Template type not found for " + request.getTypeKey()));

        documentRequirementRepository.deleteAll(templateType.getDocumentRequirements());
        templateType.getDocumentRequirements().clear();

        int order = 0;
        for (TemplateDocDto docDto : request.getDocuments()) {
            DocumentRequirement newReq = new DocumentRequirement();
            newReq.setName(docDto.getName());
            newReq.setDescription(docDto.getDescription());
            newReq.setRequired(docDto.isRequired());
            newReq.setValidityMonths(docDto.getValidityMonths());
            newReq.setDisplayOrder(order++);
            newReq.setCategory(docDto.getCategory());
            newReq.setTemplateType(templateType);
            templateType.getDocumentRequirements().add(newReq);
        }

        templateTypeRepository.save(templateType);
        return getDocumentRequirements();
    }
}