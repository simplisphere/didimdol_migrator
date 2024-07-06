package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
import com.simplisphere.didimdolstandardize.postgresql.repositories.DiagnosisRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class StandardizedRunner implements ApplicationRunner {

    private final StandardizeSosulService standardizeSosulService;
    private final HospitalRepository targetHospitalRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final RuleRepository ruleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Didimdol Standardize is running...");
        sosulStandardize();
    }

    private void sosulStandardize() {
        Instant start = Instant.now();

        log.info("병원 데이터 생성");
        Hospital hospital = preparedHospital();
        log.info("표준화 Diagnosis 생성");
        prepareDiagnosis();
        log.info("Diagnosis 표준화 룰 생성");
        prepareStandardizationRule(hospital);
        log.info("환자 데이터 표준화");
        standardizePatientInfo(hospital);
        log.info("차트 데이터 표준화");
        standardizeChartInfo(hospital);

        Instant end = Instant.now();
        log.info("Start time: {}", LocalDateTime.ofInstant(start, ZoneId.systemDefault()));
        log.info("End time: {}", LocalDateTime.ofInstant(end, ZoneId.systemDefault()));
        Duration timeElapsed = Duration.between(start, end);
        log.info("Time taken: {} s", timeElapsed.toSeconds());
    }

    private Hospital preparedHospital() {
        return targetHospitalRepository.findByName("소설").orElseGet(() -> targetHospitalRepository.save(Hospital.builder()
                .name("소설")
                .address("경기도 성남시 분당구 판교")
                .phone("031-123-4567")
                .build()
        ));
    }

    private void standardizePatientInfo(Hospital hospital) {
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by("id").ascending());
        Page<Patient> newPatients = standardizeSosulService.standardizedPatient(hospital, pageRequest);
        int completed = 0;
        while (newPatients.getPageable().getPageNumber() < newPatients.getTotalPages()) {
            completed += newPatients.getNumberOfElements();
            log.info("patient 총 {} 중 {} 저장 완료", newPatients.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newPatients = standardizeSosulService.standardizedPatient(hospital, pageRequest);
        }

        log.info("original pet count: {}", newPatients.getTotalElements());
    }

    private void standardizeChartInfo(Hospital hospital) {
        PageRequest pageRequest = PageRequest.of(0, 1000, Sort.by("id").ascending());
        Page<Chart> newCharts = standardizeSosulService.standardizedChart(hospital, pageRequest);
        int completed = 0;
        while (newCharts.getPageable().getPageNumber() < newCharts.getTotalPages()) {
            completed += newCharts.getNumberOfElements();
            log.info("chart 총 {} 중 {} 저장 완료", newCharts.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newCharts = standardizeSosulService.standardizedChart(hospital, pageRequest);
        }

        log.info("original chart count: {}", newCharts.getTotalElements());
    }

    // 표준 진단 데이터 생성
    private void prepareDiagnosis() {
        diagnosisRepository.save(Diagnosis.builder().code("A1001").description("표준화 신장 질환").name("신장 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisRepository.save(Diagnosis.builder().code("B1001").description("표준화 당뇨 질환").name("당뇨 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisRepository.save(Diagnosis.builder().code("C1001").description("표준화 비만 질환").name("비만 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisRepository.save(Diagnosis.builder().code("D1001").description("표준화 심장 질환").name("심장 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
    }

    // 표준화 룰 생성
    private void prepareStandardizationRule(Hospital hospital) {
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 1").description("신장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("만성신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 2").description("신장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("신장병").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 3").description("신장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("다낭성 신장, 간 질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 4").description("신장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 5").description("신장 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("우측신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 6").description("신장 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("신장 결석(우측)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 룰 7").description("신장 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("만성 신장 질환 (신부전 포함)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
    }
}
