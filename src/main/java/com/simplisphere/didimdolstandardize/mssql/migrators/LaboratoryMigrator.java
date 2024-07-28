package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.MsOriginSpecies;
import com.simplisphere.didimdolstandardize.mssql.dtos.query.LabItemDto;
import com.simplisphere.didimdolstandardize.mssql.dtos.query.LabResultDto;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabProduct;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabReference;
import com.simplisphere.didimdolstandardize.mssql.services.MsLaboratoryService;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryItemRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryTypeRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LaboratoryMigrator {
    private final MsLaboratoryService msLaboratoryService;

    private final LaboratoryTypeRepository postgresLaboratoryTypeRepository;
    private final LaboratoryItemRepository laboratoryItemRepository;
    private final PatientRepository patientRepository;

    public Page<LaboratoryType> convertLaboratoryType(Hospital hospital, Pageable pageRequest) {
        Page<MsLabProduct> legacyLaboratoryProductPage = msLaboratoryService.retrieveProducts(pageRequest);

        // original LabProduct -> Standardized LaboratoryType 변환
        List<LaboratoryType> laboratoryTypes = legacyLaboratoryProductPage.stream().parallel()
                .map(product -> LaboratoryType.builder()
                        .name(product.getName())
                        .originalId(product.getId().toString())
                        .abbreviation(product.getName())
                        .description("")
                        .hospital(hospital).build()
                ).toList();

        return new PageImpl<>(laboratoryTypes, legacyLaboratoryProductPage.getPageable(), legacyLaboratoryProductPage.getTotalElements());
    }

    public Page<LaboratoryItem> convertLabItem(Hospital hospital, Pageable pageRequest) {
        // original LabItem list 조회
        Page<LabItemDto> legacyItemDtoPage = msLaboratoryService.retrieveItems(pageRequest);

        // 필요한 LabProduct의 ID를 Set으로 수집
        Set<String> productIds = legacyItemDtoPage.stream()
                .map(item -> item.getProductId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryType을 미리 조회하여 맵으로 저장
        Map<String, LaboratoryType> laboratoryTypeMap = postgresLaboratoryTypeRepository.findByOriginalIdIn(productIds).stream()
                .collect(Collectors.toMap(LaboratoryType::getOriginalId, labType -> labType));

        // original LabItem -> Standardized LaboratoryItem 변환
        List<LaboratoryItem> newLaboratoryItems = legacyItemDtoPage.stream().parallel().map(itemDto -> {
            LaboratoryType laboratoryType = laboratoryTypeMap.get(itemDto.getProductId().toString());
            if (laboratoryType == null) {
                throw new RuntimeException("LaboratoryType is not found for id: " + itemDto.getProductId().toString());
            }
            return LaboratoryItem.builder()
                    .code(itemDto.getProductCode().trim() + StringUtils.leftPad(itemDto.getListOrder().toString(), 2, "0"))
                    .name(itemDto.getName())
                    .abbreviation(itemDto.getName())
                    .unit(itemDto.getUnit().trim())
                    .type(laboratoryType)
                    .orderIdx(itemDto.getListOrder())
                    .originalId(itemDto.getId().toString())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(newLaboratoryItems, legacyItemDtoPage.getPageable(), legacyItemDtoPage.getTotalElements());
    }

    public Page<LaboratoryResult> convertLabResult(Hospital hospital, Pageable pageRequest) {
        // original LabResult list 조회
        Page<LabResultDto> legacyResultDtoPage = msLaboratoryService.getLaboratoryResults(pageRequest);

        // 필요한 LabItem의 ID와 Patient의 ID를 Set으로 수집
        Set<Pair<String, String>> legacyLabItemAndTypeIds = legacyResultDtoPage.stream()
                .map(result -> Pair.of(result.getLaboratoryItemId().toString(), result.getLaboratoryTypeId().toString()))
                .collect(Collectors.toSet());

        log.trace("legacyLabItemAndTypeIds: {}", legacyLabItemAndTypeIds.size());
        log.trace("legacyLabItemAndTypeIds: {}", legacyLabItemAndTypeIds);

        Set<String> patientIds = legacyResultDtoPage.stream()
                .map(result -> result.getPetId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryItem을 미리 조회하여 맵으로 저장
        Map<Pair<String, String>, LaboratoryItem> laboratoryItemMap = laboratoryItemRepository
                .findByLegacyLabItemIdAndLegacyLabTypeIdIn(legacyLabItemAndTypeIds)
                .stream()
                .collect(Collectors.toMap(item -> Pair.of(item.getOriginalId(), item.getType().getOriginalId()), item -> item));

        log.trace("laboratoryItemMap: {}", laboratoryItemMap.size());
        log.trace("laboratoryItemMap: {}", laboratoryItemMap.keySet());

        legacyLabItemAndTypeIds.forEach(pair -> {
            if (!laboratoryItemMap.containsKey(pair)) {
                log.warn("LaboratoryItem is not found for pair: {}", pair);
            }
        });

        // 모든 필요한 Patient를 미리 조회하여 맵으로 저장
        Map<String, Patient> patientMap = patientRepository.findByOriginalIdIn(patientIds).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        // original LabResult -> LaboratoryResult 변환
        List<LaboratoryResult> laboratoryResults = legacyResultDtoPage.stream().parallel().map(result -> {
            LaboratoryItem item = laboratoryItemMap.get(Pair.of(result.getLaboratoryItemId().toString(), result.getLaboratoryTypeId().toString()));
            Patient patient = patientMap.get(result.getPetId().toString());
            return LaboratoryResult.builder()
                    .description("")
                    .result(result.getLaboratoryResultResult().trim())
                    .created(result.getLaboratoryResultCreated())
                    .patient(patient)
                    .laboratoryItem(item)
                    .originalId(result.getOriginalId())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(laboratoryResults, legacyResultDtoPage.getPageable(), legacyResultDtoPage.getTotalElements());
    }

    public Page<LaboratoryReference> convertLabReference(Hospital hospital, Pageable pageRequest) {
        List<Integer> speciesIds = List.of(MsOriginSpecies.Canine.getId(), MsOriginSpecies.Feline.getId());

        // original LabRange list 조회
        Page<MsLabReference> legacyReferences = msLaboratoryService.retrieveReferences(pageRequest, speciesIds);

        // 필요한 LabItem의 ID를 Set으로 수집
        Set<String> labItemIds = legacyReferences.stream()
                .map(range -> range.getItemId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryItem을 미리 조회하여 맵으로 저장
        Map<String, List<LaboratoryItem>> laboratoryItemMap = laboratoryItemRepository.findByOriginalIdIn(labItemIds).stream()
                .collect(Collectors.groupingBy(LaboratoryItem::getOriginalId));

        log.trace("laboratoryItemMap: {}", laboratoryItemMap);

        // original MsLabReference -> LaboratoryReference 변환
        List<LaboratoryReference> laboratoryReferences = legacyReferences.stream()
                .flatMap(legacyReference -> {
                    List<LaboratoryItem> items = laboratoryItemMap.getOrDefault(legacyReference.getItemId().toString(), Collections.emptyList());
                    return items.stream().map(item -> LaboratoryReference.builder()
                            .species(MsOriginSpecies.of(legacyReference.getSpeciesId()).toSpecies())
                            .fromAge(0)
                            .toAge(999)
                            .minReferenceRange(legacyReference.getMin().trim())
                            .maxReferenceRange(legacyReference.getMax().trim())
                            .laboratoryItem(item)
                            .originalId(legacyReference.getOriginalId())
                            .hospital(hospital)
                            .build());
                }).collect(Collectors.toList());

        return new PageImpl<>(laboratoryReferences, legacyReferences.getPageable(), legacyReferences.getTotalElements());
    }
}
