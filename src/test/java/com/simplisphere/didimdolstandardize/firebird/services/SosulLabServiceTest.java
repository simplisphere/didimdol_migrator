package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.postgresql.StandardizedLabService;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Slf4j
@SpringBootTest
@Order(2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SosulLabServiceTest {
    private static Hospital hospital;
    @Autowired
    private SosulLabService sosulLabService;

    @BeforeAll
    static void setUp(@Autowired HospitalRepository repository) {
        hospital = repository.findByName("소설").orElseGet(() -> repository.save(Hospital.builder()
                .name("소설")
                .address("경기도 성남시 분당구 판교")
                .phone("031-123-4567")
                .build()
        ));
    }

    @Test
    @Order(1)
    void convertStandardizedLabTypeFromOriginalLabProduct(@Autowired StandardizedLabService laboratoryLabService) {
        Sort sort = Sort.by(Sort.Order.asc("labProductId"));
        PageRequest pageRequest = PageRequest.of(0, 100, sort);
        Page<LaboratoryType> newLaboratorytypes = sosulLabService.convertStandardizedLabTypeFromOriginalLabProduct(hospital, pageRequest);
        laboratoryLabService.saveLaboratoryTypes(newLaboratorytypes.getContent());

        int completed = 0;
        while (newLaboratorytypes.getPageable().getPageNumber() < newLaboratorytypes.getTotalPages()) {
            laboratoryLabService.saveLaboratoryTypes(newLaboratorytypes.getContent());
            completed += newLaboratorytypes.getNumberOfElements();
            log.info("Laboratory Type 총 {} 중 {} 저장 완료", newLaboratorytypes.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newLaboratorytypes = sosulLabService.convertStandardizedLabTypeFromOriginalLabProduct(hospital, pageRequest);
        }
    }

    @Test
    @Order(2)
    void convertStandardizedLabItemFromOriginalLabItem(@Autowired StandardizedLabService laboratoryLabService) {
        Sort sort = Sort.by(Sort.Order.asc("labItemId"));
        PageRequest pageRequest = PageRequest.of(0, 200, sort);
        Page<LaboratoryItem> newLaboratoryItems = sosulLabService.convertStandardizedLabItemFromOriginalLabItem(hospital, pageRequest);
        laboratoryLabService.saveLaboratoryItems(newLaboratoryItems.getContent());

        int completed = 0;
        while (newLaboratoryItems.getPageable().getPageNumber() < newLaboratoryItems.getTotalPages()) {
            laboratoryLabService.saveLaboratoryItems(newLaboratoryItems.getContent());
            completed += newLaboratoryItems.getNumberOfElements();
            log.info("Laboratory Item 총 {} 중 {} 저장 완료", newLaboratoryItems.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newLaboratoryItems = sosulLabService.convertStandardizedLabItemFromOriginalLabItem(hospital, pageRequest);
        }
    }

    @Test
    @Order(3)
    void generateStandardizedLabRef(@Autowired StandardizedLabService laboratoryLabService) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 200, sort);
        Page<LaboratoryReference> newLaboratoryRefs = sosulLabService.convertStandardizedLabReferenceFromOriginalLabRange(hospital, pageRequest);
        laboratoryLabService.saveLaboratoryReferences(newLaboratoryRefs.getContent());

        int completed = 0;
        while (newLaboratoryRefs.getPageable().getPageNumber() < newLaboratoryRefs.getTotalPages()) {
            laboratoryLabService.saveLaboratoryReferences(newLaboratoryRefs.getContent());
            completed += newLaboratoryRefs.getNumberOfElements();
            log.info("Laboratory Reference 총 {} 중 {} 저장 완료", newLaboratoryRefs.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newLaboratoryRefs = sosulLabService.convertStandardizedLabReferenceFromOriginalLabRange(hospital, pageRequest);
        }
    }

    @Test
    @Order(4)
    void generateStandardizedLabResult(@Autowired StandardizedLabService laboratoryLabService) {
        Sort sort = Sort.by(Sort.Order.asc("labResultId"));
        PageRequest pageRequest = PageRequest.of(0, 500, sort);
        Page<LaboratoryResult> newLaboratoryResults = sosulLabService.convertLaboratoryResultFromOriginalLabResult(hospital, pageRequest);
        laboratoryLabService.saveLaboratoryResults(newLaboratoryResults.getContent());

        int completed = 0;
        while (newLaboratoryResults.getPageable().getPageNumber() < newLaboratoryResults.getTotalPages()) {
            laboratoryLabService.saveLaboratoryResults(newLaboratoryResults.getContent());
            completed += newLaboratoryResults.getNumberOfElements();
            log.info("Laboratory Result 총 {} 중 {} 저장 완료", newLaboratoryResults.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newLaboratoryResults = sosulLabService.convertLaboratoryResultFromOriginalLabResult(hospital, pageRequest);
        }
    }
}