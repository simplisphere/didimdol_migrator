package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Vital;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.VitalRepository;
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
class SosulVitalServiceTest {
    private static Hospital hospital;
    @Autowired
    private SosulVitalService sosulVitalService;

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
    void convertVitalFromSosulVital(@Autowired VitalRepository vitalRepository) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 500, sort);
        Page<Vital> newVitals = sosulVitalService.convertVitalFromSosulVital(hospital, pageRequest);
        vitalRepository.saveAll(newVitals.getContent());

        int completed = 0;
        while (newVitals.getPageable().getPageNumber() < newVitals.getTotalPages()) {
            vitalRepository.saveAll(newVitals.getContent());
            completed += newVitals.getNumberOfElements();
            log.info("Vital 총 {} 중 {} 저장 완료", newVitals.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newVitals = sosulVitalService.convertVitalFromSosulVital(hospital, pageRequest);
        }
    }
}