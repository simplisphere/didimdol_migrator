package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PrescriptionRepository;
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
class SosulPrescriptionServiceTest {

    private static Hospital hospital;
    @Autowired
    private SosulPrescriptionService sosulPrescriptionService;

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
    void convertPrescriptionFromHospitalPrescription(@Autowired PrescriptionRepository prescriptionRepository) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 1000, sort);
        Page<Prescription> prescriptions = sosulPrescriptionService.convertPrescriptionFromHospitalPrescription(hospital, pageRequest);
        prescriptionRepository.saveAll(prescriptions.getContent());

        int completed = 0;
        while (prescriptions.getPageable().getPageNumber() < prescriptions.getTotalPages()) {
            prescriptionRepository.saveAll(prescriptions.getContent());
            completed += prescriptions.getNumberOfElements();
            log.info("Prescription 총 {} 중 {} 저장 완료", prescriptions.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            prescriptions = sosulPrescriptionService.convertPrescriptionFromHospitalPrescription(hospital, pageRequest);
        }
    }
}