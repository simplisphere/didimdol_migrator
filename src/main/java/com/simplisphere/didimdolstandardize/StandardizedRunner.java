package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Didimdol Standardize is running...");
        sosulStandardize();
    }

    private void sosulStandardize() {
        Instant start = Instant.now();

        log.info("병원 데이터 생성");
        Hospital hospital = preparedHospital();
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
        PageRequest pageRequest = PageRequest.of(0, 200, Sort.by("id").ascending());
        Page<Patient> newPatients = standardizeSosulService.standardizedPatient(hospital, pageRequest);
        int completed = 0;
        while (newPatients.hasNext()) {
            completed += newPatients.getNumberOfElements();
            log.info("총 {} 중 {} 저장 완료", newPatients.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newPatients = standardizeSosulService.standardizedPatient(hospital, pageRequest);
        }

        log.info("original pet count: {}", newPatients.getTotalElements());
    }

    private void standardizeChartInfo(Hospital hospital) {
        PageRequest pageRequest = PageRequest.of(0, 500, Sort.by("id").ascending());
        Page<Chart> newCharts = standardizeSosulService.standardizedChart(hospital, pageRequest);
        int completed = 0;
        while (newCharts.hasNext()) {
            completed += newCharts.getNumberOfElements();
            log.info("총 {} 중 {} 저장 완료", newCharts.getTotalElements(), completed);
            pageRequest = pageRequest.next();
            newCharts = standardizeSosulService.standardizedChart(hospital, pageRequest);
        }

        log.info("original chart count: {}", newCharts.getTotalElements());
    }
}
