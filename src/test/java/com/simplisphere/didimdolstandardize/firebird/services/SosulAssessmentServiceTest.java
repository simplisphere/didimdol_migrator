package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Assessment;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.repositories.AssessmentRepository;
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
@Order(3)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SosulAssessmentServiceTest {
    private static Hospital hospital;
    @Autowired
    private SosulAssessmentService sosulAssessmentService;

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
    void convertAssessmentFromOriginalAssessment(@Autowired AssessmentRepository assessmentRepository) {
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 50, sort);
        Page<Assessment> assessments = sosulAssessmentService.convertAssessmentFromOriginalAssessment(hospital, pageRequest);
        assessmentRepository.saveAll(assessments.getContent());

        int completed = 0;
        while (assessments.getPageable().getPageNumber() < assessments.getTotalPages()) {
            assessmentRepository.saveAll(assessments.getContent());
            completed += assessments.getNumberOfElements();
            log.info("Assessment 총 {} 중 {} 저장 완료", assessments.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            assessments = sosulAssessmentService.convertAssessmentFromOriginalAssessment(hospital, pageRequest);
        }
    }
}