package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabItem;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabProduct;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabRange;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabResult;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulLabItemRepository;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulLabProductRepository;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulLabRangeRepository;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulLabResultRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulLabService {
    private final SosulLabResultRepository sosulLabResultRepository;
    private final SosulLabProductRepository sosulLabProductRepository;
    private final SosulLabItemRepository sosulLabItemRepository;
    private final SosulLabRangeRepository sosulLabRangeRepository;

    private final LaboratoryTypeRepository postgresLaboratoryTypeRepository;
    private final LaboratoryItemRepository laboratoryItemRepository;
    private final PatientRepository patientRepository;

    // convert laboratory type from original lab product
    public Page<LaboratoryType> convertStandardizedLabTypeFromOriginalLabProduct(Hospital hospital, PageRequest pageRequest) {
        // original LabProduct list 조회
        Page<SosulLabProduct> sosulLabProducts = sosulLabProductRepository.findAll(pageRequest);

        // original LabProduct -> Standardized LaboratoryType 변환
        List<LaboratoryType> laboratoryTypes = sosulLabProducts.stream().parallel()
                .map(product -> LaboratoryType.builder()
                        .name(product.getLabProductName()).originalId(product.getLabProductId().toString()).name(product.getLabProductName())
                        .abbreviation(product.getLabProductName()).description(product.getLabProductCompany()).hospital(hospital).build()
                ).toList();

        return new PageImpl<>(laboratoryTypes, sosulLabProducts.getPageable(), sosulLabProducts.getTotalElements());
    }

    // convert laboratory item from original lab item
    public Page<LaboratoryItem> convertStandardizedLabItemFromOriginalLabItem(Hospital hospital, PageRequest pageRequest) {
        // original LabItem list 조회
        Page<SosulLabItem> sosulLabItems = sosulLabItemRepository.findAll(pageRequest);

        // 필요한 LabProduct의 ID를 Set으로 수집
        Set<String> labProductIds = sosulLabItems.stream()
                .map(item -> item.getLabProduct().getLabProductId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryType을 미리 조회하여 맵으로 저장
        Map<String, LaboratoryType> laboratoryTypeMap = postgresLaboratoryTypeRepository.findByOriginalIdIn(labProductIds).stream()
                .collect(Collectors.toMap(LaboratoryType::getOriginalId, labType -> labType));

        // original LabItem -> Standardized LaboratoryItem 변환
        List<LaboratoryItem> newLaboratoryItems = sosulLabItems.stream().parallel().map(originalItem -> {
            LaboratoryType laboratoryType = laboratoryTypeMap.get(originalItem.getLabProduct().getLabProductId().toString());
            if (laboratoryType == null) {
                throw new RuntimeException("LaboratoryType is not found for id: " + originalItem.getLabProduct().getLabProductId().toString());
            }
            return LaboratoryItem.builder()
                    .code(originalItem.getLabItemCode())
                    .name(originalItem.getLabItemName())
                    .abbreviation(originalItem.getLabItemAbbr())
                    .unit(originalItem.getLabItemUnit())
                    .type(laboratoryType)
                    .orderIdx(originalItem.getOrderIdx())
                    .originalId(originalItem.getLabItemId().toString())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(newLaboratoryItems, sosulLabItems.getPageable(), sosulLabItems.getTotalElements());
    }

    // convert laboratory reference from original lab range
    public Page<LaboratoryReference> convertStandardizedLabReferenceFromOriginalLabRange(Hospital hospital, PageRequest pageRequest) {
        // original LabRange list 조회
        Page<SosulLabRange> sosulLabRanges = sosulLabRangeRepository.findAll(pageRequest);

        // 필요한 LabItem의 ID를 Set으로 수집
        Set<String> labItemIds = sosulLabRanges.stream()
                .map(range -> range.getLabItem().getLabItemId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryItem을 미리 조회하여 맵으로 저장
        Map<String, LaboratoryItem> laboratoryItemMap = laboratoryItemRepository.findByOriginalIdIn(labItemIds).stream()
                .collect(Collectors.toMap(LaboratoryItem::getOriginalId, item -> item));

        // original LabRange -> LaboratoryReference 변환
        List<LaboratoryReference> laboratoryReferences = sosulLabRanges.stream().parallel().map(range -> {
            LaboratoryItem item = laboratoryItemMap.get(range.getLabItem().getLabItemId().toString());
            return LaboratoryReference.builder()
                    .species(range.getSpecies().toSpecies())
                    .fromAge(range.getAgeFrom())
                    .toAge(range.getAgeTo())
                    .minReferenceRange(range.getMin())
                    .maxReferenceRange(range.getMax())
                    .laboratoryItem(item)
                    .originalId(range.getId().toString())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(laboratoryReferences, sosulLabRanges.getPageable(), sosulLabRanges.getTotalElements());
    }

    // convert lab result from original lab result
    public Page<LaboratoryResult> convertLaboratoryResultFromOriginalLabResult(Hospital hospital, PageRequest pageRequest) {
        // original LabResult list 조회
        Page<SosulLabResult> sosulLabResults = sosulLabResultRepository.findAll(pageRequest);

        // 필요한 LabItem의 ID와 Patient의 ID를 Set으로 수집
        Set<String> labItemIds = sosulLabResults.stream()
                .map(result -> result.getLabItem().getLabItemId().toString())
                .collect(Collectors.toSet());

        Set<String> patientIds = sosulLabResults.stream()
                .map(result -> result.getLabDate().getPet().getId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 LaboratoryItem을 미리 조회하여 맵으로 저장
        Map<String, LaboratoryItem> laboratoryItemMap = laboratoryItemRepository.findByOriginalIdIn(labItemIds).stream()
                .collect(Collectors.toMap(LaboratoryItem::getOriginalId, item -> item));

        // 모든 필요한 Patient를 미리 조회하여 맵으로 저장
        Map<String, Patient> patientMap = patientRepository.findByOriginalIdIn(patientIds).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        // original LabResult -> LaboratoryResult 변환
        List<LaboratoryResult> laboratoryResults = sosulLabResults.stream().parallel().map(result -> {
            LaboratoryItem item = laboratoryItemMap.get(result.getLabItem().getLabItemId().toString());
            Patient patient = patientMap.get(result.getLabDate().getPet().getId().toString());
            return LaboratoryResult.builder()
                    .description(result.getLabResultDesc())
                    .result(result.getLabResultValue())
                    .created(result.getLabDate().getLabDateDate().atTime(result.getLabDate().getLabDateTime()))
                    .patient(patient)
                    .laboratoryItem(item)
                    .originalId(result.getLabResultId().toString())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(laboratoryResults, sosulLabResults.getPageable(), sosulLabResults.getTotalElements());
    }
}
