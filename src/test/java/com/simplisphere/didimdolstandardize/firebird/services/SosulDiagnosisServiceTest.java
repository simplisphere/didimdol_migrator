package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.HospitalDiagnosis;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalDiagnosisRepository;
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
@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SosulDiagnosisServiceTest {
    private static Hospital hospital;
    @Autowired
    private SosulDiagnosisService sosulDiagnosisService;

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
    void saveHospitalDiagnosisAndSaveStandardDiagnosisFromSosulDiagnosis(@Autowired HospitalDiagnosisRepository hospitalDiagnosisRepository) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 50, sort);
        Page<HospitalDiagnosis> hospitalDiagnoses = sosulDiagnosisService.saveHospitalDiagnosisAndSaveStandardDiagnosisFromSosulDiagnosis(hospital, pageRequest);
        hospitalDiagnosisRepository.saveAll(hospitalDiagnoses.getContent());

        int completed = 0;
        while (hospitalDiagnoses.getPageable().getPageNumber() < hospitalDiagnoses.getTotalPages()) {
            hospitalDiagnosisRepository.saveAll(hospitalDiagnoses.getContent());
            completed += hospitalDiagnoses.getNumberOfElements();
            log.info("Laboratory Item 총 {} 중 {} 저장 완료", hospitalDiagnoses.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            hospitalDiagnoses = sosulDiagnosisService.saveHospitalDiagnosisAndSaveStandardDiagnosisFromSosulDiagnosis(hospital, pageRequest);
        }
    }
}