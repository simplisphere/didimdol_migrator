package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabItem;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabProduct;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabRange;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabResult;
import com.simplisphere.didimdolstandardize.firebird.repositories.*;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import com.simplisphere.didimdolstandardize.postgresql.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulLabService {
    private final SosulLabResultRepository sosulLabResultRepository;
    private final SosulLabProductRepository sosulLabProductRepository;
    private final SosulLabItemRepository sosulLabItemRepository;
    private final SosulLabRangeRepository sosulLabRangeRepository;

    private final LaboratoryTypeRepository postgresLaboratoryTypeRepository;
    private final SosulDxRepository sosulDxRepository;
    private final HospitalDiagnosisRepository hospitalDiagnosisRepository;
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;
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

        // original LabItem -> Standardized LaboratoryItem 변환
        List<LaboratoryItem> newLaboratoryItems = sosulLabItems.stream().parallel().map(originalItem -> {
            Optional<LaboratoryType> LaboratoryType = postgresLaboratoryTypeRepository.findByOriginalId(originalItem.getLabProduct().getLabProductId().toString());
            return LaboratoryItem.builder()
                    .code(originalItem.getLabItemCode()).name(originalItem.getLabItemName()).abbreviation(originalItem.getLabItemAbbr())
                    .unit(originalItem.getLabItemUnit()).type(LaboratoryType.orElseThrow(() -> new RuntimeException("LaboratoryType is not found")))
                    .orderIdx(originalItem.getOrderIdx()).originalId(originalItem.getLabItemId().toString()).hospital(hospital).build();
        }).toList();

        return new PageImpl<>(newLaboratoryItems, sosulLabItems.getPageable(), sosulLabItems.getTotalElements());
    }

    // convert laboratory reference from original lab range
    public Page<LaboratoryReference> convertStandardizedLabReferenceFromOriginalLabRange(Hospital hospital, PageRequest pageRequest) {
        // original LabRange list 조회
        Page<SosulLabRange> sosulLabRanges = sosulLabRangeRepository.findAll(pageRequest);

        // original LabRange -> LaboratoryReference
        List<LaboratoryReference> laboratoryReferences = sosulLabRanges.stream().parallel().map(range -> {
            Optional<LaboratoryItem> item = laboratoryItemRepository.findByOriginalId(range.getLabItem().getLabItemId().toString());
            return LaboratoryReference.builder()
                    .species(range.getSpecies().toSpecies())
                    .fromAge(range.getAgeFrom())
                    .toAge(range.getAgeTo())
                    .minReferenceRange(range.getMin())
                    .maxReferenceRange(range.getMax())
                    .laboratoryItem(item.orElse(null))
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

        // original LabResult -> LaboratoryResult
        List<LaboratoryResult> laboratoryResults = sosulLabResults.stream().parallel().map(result -> {
            Optional<LaboratoryItem> item = laboratoryItemRepository.findByOriginalId(result.getLabItem().getLabItemId().toString());
            Patient patient = patientRepository.findByOriginalId(result.getLabDate().getPet().getId().toString());
            return LaboratoryResult.builder()
                    .description(result.getLabResultDesc())
                    .result(result.getLabResultValue())
                    .created(result.getLabDate().getLabDateDate().atTime(result.getLabDate().getLabDateTime()))
                    .patient(patient)
                    .laboratoryItem(item.orElse(null))
                    .originalId(result.getLabResultId().toString())
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(laboratoryResults, sosulLabResults.getPageable(), sosulLabResults.getTotalElements());
    }
}
