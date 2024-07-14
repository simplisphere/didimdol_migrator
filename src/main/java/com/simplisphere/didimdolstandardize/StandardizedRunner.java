package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.postgresql.MarkerType;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.repositories.*;
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
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class StandardizedRunner implements ApplicationRunner {

    private final StandardizeSosulService standardizeSosulService;
    private final HospitalRepository targetHospitalRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final RuleRepository ruleRepository;
    private final MedicineRepository medicineRepository;
    private final StandardMarkerRepository standardMarkerRepository;

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
        prepareStandardizationDiagnosisRule(hospital);
        log.info("표준화 약품 생성");
        prepareMedicine();
        log.info("약품 표준화 룰 생성");
        prepareStandardizationMedicationRule(hospital);
        log.info("표준화 마커 생성");
        prepareStandardizedMarker();
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

    // 표준화 Diagnosis 룰 생성
    private void prepareStandardizationDiagnosisRule(Hospital hospital) {
        // 신장 질환
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 1").description("신장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("만성신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 2").description("신장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("신장병").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 3").description("신장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("다낭성 신장, 간 질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 4").description("신장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 5").description("신장 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("우측신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 6").description("신장 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("신장 결석(우측)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 7").description("신장 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("만성 신장 질환 (신부전 포함)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 8").description("신장 질환 표준화 룰 8").type(RuleType.DIAGNOSIS).fromName("다낭신장질환, 원발만성신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 9").description("신장 질환 표준화 룰 9").type(RuleType.DIAGNOSIS).fromName("우측 신장경색").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 10").description("신장 질환 표준화 룰 10").type(RuleType.DIAGNOSIS).fromName("원발신장질환 iris stage1-2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 11").description("신장 질환 표준화 룰 11").type(RuleType.DIAGNOSIS).fromName("원발신장질환 stage2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 13").description("신장 질환 표준화 룰 13").type(RuleType.DIAGNOSIS).fromName("우측신장위축").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 14").description("신장 질환 표준화 룰 14").type(RuleType.DIAGNOSIS).fromName("우측신장 위축").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 15").description("신장 질환 표준화 룰 15").type(RuleType.DIAGNOSIS).fromName("만성신장질환-희석뇨 & 저칼륨혈증 의심됨").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 16").description("신장 질환 표준화 룰 16").type(RuleType.DIAGNOSIS).fromName("만성신장병").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 17").description("신장 질환 표준화 룰 17").type(RuleType.DIAGNOSIS).fromName("다낭신장병").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 18").description("신장 질환 표준화 룰 18").type(RuleType.DIAGNOSIS).fromName("원발신장질환 iris stage3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 19").description("신장 질환 표준화 룰 19").type(RuleType.DIAGNOSIS).fromName("신장질환-희석뇨, 신장수치 증가").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 20").description("신장 질환 표준화 룰 20").type(RuleType.DIAGNOSIS).fromName("신장결석 - 양측").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 21").description("신장 질환 표준화 룰 21").type(RuleType.DIAGNOSIS).fromName("원발만성신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 22").description("신장 질환 표준화 룰 22").type(RuleType.DIAGNOSIS).fromName("신장 결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 23").description("신장 질환 표준화 룰 23").type(RuleType.DIAGNOSIS).fromName("우측신장위축, 좌측cyst").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 24").description("신장 질환 표준화 룰 24").type(RuleType.DIAGNOSIS).fromName("신장위축").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 25").description("신장 질환 표준화 룰 25").type(RuleType.DIAGNOSIS).fromName("신장 수질 경계부 불명확").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 26").description("신장 질환 표준화 룰 26").type(RuleType.DIAGNOSIS).fromName("신장요관결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 27").description("신장 질환 표준화 룰 27").type(RuleType.DIAGNOSIS).fromName("좌측신우확장, 양쪽신장 cm junction loss").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 28").description("신장 질환 표준화 룰 28").type(RuleType.DIAGNOSIS).fromName("신장 변연부 irregular").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 29").description("신장 질환 표준화 룰 29").type(RuleType.DIAGNOSIS).fromName("양쪽 신장 CM junction 소실").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 30").description("신장 질환 표준화 룰 30").type(RuleType.DIAGNOSIS).fromName("좌신결석, 신장실루엣 변화").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 31").description("신장 질환 표준화 룰 31").type(RuleType.DIAGNOSIS).fromName("양쪽모두 신장결석 및 석회화").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 32").description("신장 질환 표준화 룰 32").type(RuleType.DIAGNOSIS).fromName("양쪽 신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 33").description("신장 질환 표준화 룰 33").type(RuleType.DIAGNOSIS).fromName("신장기능저하").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 34").description("신장 질환 표준화 룰 34").type(RuleType.DIAGNOSIS).fromName("양측신장결석, 우신신우확장").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 35").description("신장 질환 표준화 룰 35").type(RuleType.DIAGNOSIS).fromName("우측 신장 결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 36").description("신장 질환 표준화 룰 36").type(RuleType.DIAGNOSIS).fromName("좌측신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 37").description("신장 질환 표준화 룰 37").type(RuleType.DIAGNOSIS).fromName("양측신장 결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 38").description("신장 질환 표준화 룰 38").type(RuleType.DIAGNOSIS).fromName("우측 신장 위축").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 39").description("신장 질환 표준화 룰 39").type(RuleType.DIAGNOSIS).fromName("양측 신장 결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 40").description("신장 질환 표준화 룰 40").type(RuleType.DIAGNOSIS).fromName("만성신장질환w/신결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 41").description("신장 질환 표준화 룰 41").type(RuleType.DIAGNOSIS).fromName("ckd iris stage 2 - 양쪽 신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 42").description("신장 질환 표준화 룰 42").type(RuleType.DIAGNOSIS).fromName("신장결석, 변연부 변형").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 43").description("신장 질환 표준화 룰 43").type(RuleType.DIAGNOSIS).fromName("만성신장병 iris stage1").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 44").description("신장 질환 표준화 룰 44").type(RuleType.DIAGNOSIS).fromName("다낭성, 사수체낭성 신장질환, 신장 낭종").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 45").description("신장 질환 표준화 룰 45").type(RuleType.DIAGNOSIS).fromName("CKD iris stage2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 46").description("신장 질환 표준화 룰 46").type(RuleType.DIAGNOSIS).fromName("CKD iris stage 2(terminal) suspected W/ CM").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 47").description("신장 질환 표준화 룰 47").type(RuleType.DIAGNOSIS).fromName("CKD w/ 좌신 신우확장").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 48").description("신장 질환 표준화 룰 48").type(RuleType.DIAGNOSIS).fromName("CKD/결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 49").description("신장 질환 표준화 룰 49").type(RuleType.DIAGNOSIS).fromName("CKD iris stage 4").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 50").description("신장 질환 표준화 룰 50").type(RuleType.DIAGNOSIS).fromName("CKD stage4").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 51").description("신장 질환 표준화 룰 51").type(RuleType.DIAGNOSIS).fromName("CKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 52").description("신장 질환 표준화 룰 52").type(RuleType.DIAGNOSIS).fromName("CKD stage1").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 53").description("신장 질환 표준화 룰 53").type(RuleType.DIAGNOSIS).fromName("CKD iris stage3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 54").description("신장 질환 표준화 룰 54").type(RuleType.DIAGNOSIS).fromName("CKD stage2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 55").description("신장 질환 표준화 룰 55").type(RuleType.DIAGNOSIS).fromName("ckd IRIS STAGE 1 SUSPECTED").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 56").description("신장 질환 표준화 룰 56").type(RuleType.DIAGNOSIS).fromName("만성콩팥질환 IRIS STAGE-").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 57").description("신장 질환 표준화 룰 57").type(RuleType.DIAGNOSIS).fromName("우측PKD suspected").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 58").description("신장 질환 표준화 룰 58").type(RuleType.DIAGNOSIS).fromName("CKD suspected").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 59").description("신장 질환 표준화 룰 59").type(RuleType.DIAGNOSIS).fromName("CKD iris stage1").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 60").description("신장 질환 표준화 룰 60").type(RuleType.DIAGNOSIS).fromName("PKD with hepatic cyst").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 61").description("신장 질환 표준화 룰 61").type(RuleType.DIAGNOSIS).fromName("CKD stage 3-4").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 63").description("신장 질환 표준화 룰 63").type(RuleType.DIAGNOSIS).fromName("PKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 64").description("신장 질환 표준화 룰 64").type(RuleType.DIAGNOSIS).fromName("CKD iris stage 2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 65").description("신장 질환 표준화 룰 65").type(RuleType.DIAGNOSIS).fromName("CKD iris stage1-2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 66").description("신장 질환 표준화 룰 66").type(RuleType.DIAGNOSIS).fromName("bilateral PKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 67").description("신장 질환 표준화 룰 67").type(RuleType.DIAGNOSIS).fromName("ckd 2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 68").description("신장 질환 표준화 룰 68").type(RuleType.DIAGNOSIS).fromName("CKD stage3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 69").description("신장 질환 표준화 룰 69").type(RuleType.DIAGNOSIS).fromName("CKD iris stage 1-2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 71").description("신장 질환 표준화 룰 71").type(RuleType.DIAGNOSIS).fromName("CKD with azotemia").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 72").description("신장 질환 표준화 룰 72").type(RuleType.DIAGNOSIS).fromName("CKD stage2-3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 73").description("신장 질환 표준화 룰 73").type(RuleType.DIAGNOSIS).fromName("CKD iris stage3-4").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 74").description("신장 질환 표준화 룰 74").type(RuleType.DIAGNOSIS).fromName("CKD iris stage3 with proteinuria").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 75").description("신장 질환 표준화 룰 75").type(RuleType.DIAGNOSIS).fromName("mutiple PKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 76").description("신장 질환 표준화 룰 76").type(RuleType.DIAGNOSIS).fromName(":CKD  iris stage 2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 77").description("신장 질환 표준화 룰 77").type(RuleType.DIAGNOSIS).fromName("PKD & CKD iris stage2-3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 78").description("신장 질환 표준화 룰 78").type(RuleType.DIAGNOSIS).fromName("CKD stage2 w/ 희석뇨").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 79").description("신장 질환 표준화 룰 79").type(RuleType.DIAGNOSIS).fromName("suspected CKD iris stage 1").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 80").description("신장 질환 표준화 룰 80").type(RuleType.DIAGNOSIS).fromName("CKD iris stage2 의증").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 81").description("신장 질환 표준화 룰 81").type(RuleType.DIAGNOSIS).fromName("CKD 4").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 82").description("신장 질환 표준화 룰 82").type(RuleType.DIAGNOSIS).fromName("ckd iris stage 3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 83").description("신장 질환 표준화 룰 83").type(RuleType.DIAGNOSIS).fromName("CKD - 희석뇨, 요독증, 고인혈증").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 84").description("신장 질환 표준화 룰 84").type(RuleType.DIAGNOSIS).fromName("CKD 2-3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 85").description("신장 질환 표준화 룰 85").type(RuleType.DIAGNOSIS).fromName("PKD & CKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 86").description("신장 질환 표준화 룰 86").type(RuleType.DIAGNOSIS).fromName("CKD iris stage2-3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 87").description("신장 질환 표준화 룰 87").type(RuleType.DIAGNOSIS).fromName("ckd iris2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 88").description("신장 질환 표준화 룰 88").type(RuleType.DIAGNOSIS).fromName("고인혈증, CKD stage3").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 89").description("신장 질환 표준화 룰 89").type(RuleType.DIAGNOSIS).fromName("방광염, 췌장염, CKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 90").description("신장 질환 표준화 룰 90").type(RuleType.DIAGNOSIS).fromName("CKD stage2 with PKD").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 91").description("신장 질환 표준화 룰 91").type(RuleType.DIAGNOSIS).fromName("희석뇨, ckd 1-2stage").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("신장 질환 92").description("신장 질환 표준화 룰 92").type(RuleType.DIAGNOSIS).fromName("CKD iris stag2 2").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 당뇨 질환
        ruleRepository.save(StandardizedRule.builder().name("당뇨 질환 1").description("당뇨 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("당뇨병").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 비만 질환
        ruleRepository.save(StandardizedRule.builder().name("비만 질환 1").description("비만 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("비만").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("비만 질환 2").description("비만 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("비만세포종- 건강검진").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 심장 질환
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 1").description("심장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("비대심근증 B1").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 2").description("심장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("비대성 심장근육병증").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 3").description("심장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("비대심근증(유두근)").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 4").description("심장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("심근증").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 5").description("심장 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("갑상샘기능항진증, 간병증. 심근질환").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 6").description("심장 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("심근질환").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("심장 질환 7").description("심장 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("비대심근증").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
    }

    // 표준 약품 데이터 생성
    private void prepareMedicine() {
        medicineRepository.save(Medicine.builder().description("표준 레메론 정").name("레메론 정").build());
        medicineRepository.save(Medicine.builder().description("표준 베나실 정").name("베나실 정").build());
        medicineRepository.save(Medicine.builder().description("표준 텔미원 정").name("텔미원 정").build());
        medicineRepository.save(Medicine.builder().description("표준 레나메진 캡슐").name("레나메진 캡슐").build());
        medicineRepository.save(Medicine.builder().description("표준 세레니아 정").name("세레니아 정").build());
        medicineRepository.save(Medicine.builder().description("표준 암로디핀 정").name("암로디핀 정").build());
        medicineRepository.save(Medicine.builder().description("표준 텔미로탄 정").name("텔미로탄 정").build());
        medicineRepository.save(Medicine.builder().description("표준 아스피린 정").name("아스피린 정").build());
        medicineRepository.save(Medicine.builder().description("표준 캐닌슐린 주사").name("캐닌슐린 주사").build());
    }

    // 표준화 Medicine 룰 생성
    private void prepareStandardizationMedicationRule(Hospital hospital) {
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 1").description("약품 맵핑 1").type(RuleType.PRESCRIPTION).fromName("S_레메론_정_30T/box").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 2").description("약품 맵핑 2").type(RuleType.PRESCRIPTION).fromName("S_베나실_정_100T/box").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 3").description("약품 맵핑 3").type(RuleType.PRESCRIPTION).fromName("S_텔미원_정_30T_box").toName("텔미원 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 4").description("약품 맵핑 4").type(RuleType.PRESCRIPTION).fromName("S_레나메진_캡슐_7cap/90포/box").toName("레나메진 캡슐").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 5").description("약품 맵핑 5").type(RuleType.PRESCRIPTION).fromName("S_세레니아_정_4T/box").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 6").description("약품 맵핑 6").type(RuleType.PRESCRIPTION).fromName("레메론_정_15.0mg_30T").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 7").description("약품 맵핑 7").type(RuleType.PRESCRIPTION).fromName("세레니아_정_24mg_4T").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 8").description("약품 맵핑 8").type(RuleType.PRESCRIPTION).fromName("베나실(benacil)_정_10mg_100T").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 9").description("약품 맵핑 9").type(RuleType.PRESCRIPTION).fromName("암로디핀_정__5mg_30T").toName("암로디핀 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 10").description("약품 맵핑 10").type(RuleType.PRESCRIPTION).fromName("텔미로탄_정_40mg_30T").toName("텔미로탄 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 11").description("약품 맵핑 11").type(RuleType.PRESCRIPTION).fromName("텔미원_정_40mg_30T").toName("텔미원 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 12").description("약품 맵핑 12").type(RuleType.PRESCRIPTION).fromName("세레니아_정_16mg_4T").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 13").description("약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("텔미로탄").toName("텔미로탄 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 14").description("약품 맵핑 14").type(RuleType.PRESCRIPTION).fromName("S_Caninsulin 40IE/ml(캐닌슐린) 10ml_주사_주사비").toName("캐닌슐린 주사").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 15").description("약품 맵핑 15").type(RuleType.PRESCRIPTION).fromName("S_아스피린_정_100T/box").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
        ruleRepository.save(StandardizedRule.builder().name("약품 맵핑 16").description("약품 맵핑 16").type(RuleType.PRESCRIPTION).fromName("아스피린_정_100mg_100T").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
    }

    // 표준화 마커 생성
    private void prepareStandardizedMarker() {

        Diagnosis diagnosis1 = diagnosisRepository.findByName("신장 질환").orElseThrow(() -> new RuntimeException("Diagnosis(신장 질환) not found"));
        Diagnosis diagnosis2 = diagnosisRepository.findByName("당뇨 질환").orElseThrow(() -> new RuntimeException("Diagnosis(당뇨 질환) not found"));
        Diagnosis diagnosis3 = diagnosisRepository.findByName("비만 질환").orElseThrow(() -> new RuntimeException("Diagnosis(비만 질환) not found"));
        Diagnosis diagnosis4 = diagnosisRepository.findByName("심장 질환").orElseThrow(() -> new RuntimeException("Diagnosis(심장 질환) not found"));

        // 신장 질환 혈액 검사 표준화 마커
        saveLaboratoryMarker(diagnosis1, "GA009", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "CH011", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "LB0056", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "BS0014", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "LB0113", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "PT0010", "BUN", Species.FELINE, "mg/dL", 10F, 30F);
        saveLaboratoryMarker(diagnosis1, "GA009", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "CH011", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "LB0056", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "BS0014", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "LB0113", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "PT0010", "BUN", Species.CANINE, "mg/dL", 8F, 31F);
        saveLaboratoryMarker(diagnosis1, "CH076", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "CH019", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "LB0063", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "BS0004", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "LB0116", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "PT0018", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F);
        saveLaboratoryMarker(diagnosis1, "CH076", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "CH019", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "LB0063", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "BS0004", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "LB0116", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "PT0018", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F);
        saveLaboratoryMarker(diagnosis1, "CH143", "SDMA", Species.FELINE, "mg/dL", 0F, 14F);
        saveLaboratoryMarker(diagnosis1, "CH143", "SDMA", Species.CANINE, "mg/dL", 0F, 15F);
        saveLaboratoryMarker(diagnosis1, "UR021", "UPC", Species.FELINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "LB0098", "UPC", Species.FELINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "CH063", "UPC", Species.FELINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "UR021", "UPC", Species.CANINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "LB0098", "UPC", Species.CANINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "CH063", "UPC", Species.CANINE, "mg/dL", 0F, 5F);
        saveLaboratoryMarker(diagnosis1, "CH044", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "EL002", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "K+", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "GA018", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "ISM002", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "CH044", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "EL002", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "K+", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "GA018", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "ISM002", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "BS0007", "P", Species.FELINE, "mg/dL", 3F, 5.5F);
        saveLaboratoryMarker(diagnosis1, "BS0007", "P", Species.CANINE, "mg/dL", 2.5F, 5F);

        // 당뇨 질환 혈액 검사 표준화 마커
        saveLaboratoryMarker(diagnosis2, "GA013", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "UR012", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "CH077", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "CH026", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "LB0071", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "BS0015", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "LB0117", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "LB0151", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "PT0005", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "UR003", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "혈당(glucose, glu)", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis2, "GA013", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "UR012", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "CH077", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "CH026", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "LB0071", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "BS0015", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "LB0117", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "LB0151", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "PT0005", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "UR003", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "혈당(glucose, glu)", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis2, "CH023", "FRUC", Species.FELINE, "μmol/L", 150F, 300F);
        saveLaboratoryMarker(diagnosis2, "SO0003", "FRUC", Species.FELINE, "μmol/L", 150F, 300F);
        saveLaboratoryMarker(diagnosis2, "CH023", "FRUC", Species.CANINE, "μmol/L", 120F, 250F);
        saveLaboratoryMarker(diagnosis2, "SO0003", "FRUC", Species.CANINE, "μmol/L", 120F, 250F);
        saveLaboratoryMarker(diagnosis2, "PT0025", "HbA1c", Species.FELINE, "%", 4F, 6F);
        saveLaboratoryMarker(diagnosis2, "c0001", "HbA1c", Species.FELINE, "%", 4F, 6F);
        saveLaboratoryMarker(diagnosis2, "PT0025", "HbA1c", Species.CANINE, "%", 4F, 6F);
        saveLaboratoryMarker(diagnosis2, "c0001", "HbA1c", Species.CANINE, "%", 4F, 6F);
        // U.GLU 검사 항목이 없음
//        saveLaboratoryMarker(diagnosis2, "", "U.GLU", Species.FELINE, "", 0F, 9999F);
//        saveLaboratoryMarker(diagnosis2, "", "U.GLU", Species.CANINE, "", 0F, 9999F);

        // 비만 질환 혈액 검사 표준화 마커
        saveLaboratoryMarker(diagnosis3, "GA013", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "UR012", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "CH077", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "CH026", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "LB0071", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "BS0015", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "LB0117", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "LB0151", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "PT0005", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "UR003", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "혈당(glucose, glu)", "GLU", Species.FELINE, "mg/dL", 0F, 150F);
        saveLaboratoryMarker(diagnosis3, "GA013", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "UR012", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "CH077", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "CH026", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "LB0071", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "BS0015", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "LB0117", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "LB0151", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "PT0005", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "UR003", "GLU", Species.CANINE, "mg/dL", 0F, 140F);
        saveLaboratoryMarker(diagnosis3, "CH105", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH003", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "LB0051", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "BS0012", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH054", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "PT0001", "ALT", Species.FELINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH105", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH003", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "LB0051", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "BS0012", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH054", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "PT0001", "ALT", Species.CANINE, "U/L", 10F, 45F);
        saveLaboratoryMarker(diagnosis3, "CH106", "AST", Species.FELINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "CH007", "AST", Species.FELINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "LB0054", "AST", Species.FELINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "BS0008", "AST", Species.FELINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "PT0002", "AST", Species.FELINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "CH106", "AST", Species.CANINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "CH007", "AST", Species.CANINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "LB0054", "AST", Species.CANINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "BS0008", "AST", Species.CANINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "PT0002", "AST", Species.CANINE, "U/L", 10F, 40F);
        saveLaboratoryMarker(diagnosis3, "LB0092", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "BS0005", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "PT0016", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "LB0092", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "BS0005", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "PT0016", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F);
        saveLaboratoryMarker(diagnosis3, "CH042", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F);
        saveLaboratoryMarker(diagnosis3, "LB0096", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F);
        saveLaboratoryMarker(diagnosis3, "PT0023", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F);
        saveLaboratoryMarker(diagnosis3, "CH042", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F);
        saveLaboratoryMarker(diagnosis3, "LB0096", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F);
        saveLaboratoryMarker(diagnosis3, "PT0023", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F);

        // 심장 질환 혈액 검사 표준화 마커
        saveLaboratoryMarker(diagnosis4, "CH116", "Troponin I ", Species.FELINE, "ng/mL", 0F, 0.16F);
        saveLaboratoryMarker(diagnosis4, "CH116", "Troponin I", Species.CANINE, "ng/mL", 0F, 0.1F);
        saveLaboratoryMarker(diagnosis4, "CH018", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F);
        saveLaboratoryMarker(diagnosis4, "CH018", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F);
        saveLaboratoryMarker(diagnosis4, "CH129", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F);
        saveLaboratoryMarker(diagnosis4, "fProBNP", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F);
        saveLaboratoryMarker(diagnosis4, "CH129", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F);
        saveLaboratoryMarker(diagnosis4, "fProBNP", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F);

        // 신장 질환 처방 표준화 마커
        List<String> diagnosis1StandardMarkers = List.of("세레니아 정", "레메론 정", "베나실 정", "텔미원 정", "레나메진 캡슐", "암로디핀 정", "텔미로탄 정");
        diagnosis1StandardMarkers.forEach(medicineName -> {
            savePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.FELINE);
            savePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.CANINE);
        });

        // 당뇨 질환 처방 표준화 마커
        List<String> diagnosis2StandardMarkers = List.of("캐닌슐린 주사");
        diagnosis2StandardMarkers.forEach(medicineName -> {
            savePrescriptionMarker(diagnosis2, medicineName, medicineName + " 처방", Species.FELINE);
            savePrescriptionMarker(diagnosis2, medicineName, medicineName + " 처방", Species.CANINE);
        });

        // 비만 질환 처방 표준화 마커
//        List<String> diagnosis3StandardMarkers = List.of("세레니아 정", "레메론 정");
//        diagnosis3StandardMarkers.forEach(medicineName -> {
//            savePrescriptionMarker(diagnosis3, medicineName, medicineName + " 처방", Species.FELINE);
//            savePrescriptionMarker(diagnosis3, medicineName, medicineName + " 처방", Species.CANINE);
//        });

        // 심장 질환 처방 표준화 마커
        List<String> diagnosis4StandardMarkers = List.of("베나실 정", "아스피린 정");
        diagnosis4StandardMarkers.forEach(medicineName -> {
            savePrescriptionMarker(diagnosis4, medicineName, medicineName + " 처방", Species.FELINE);
            savePrescriptionMarker(diagnosis4, medicineName, medicineName + " 처방", Species.CANINE);
        });
    }

    private void saveLaboratoryMarker(Diagnosis diagnosis, String code, String name, Species species, String unit, Float minRef, Float maxRef) {
        standardMarkerRepository.save(
                StandardizeDiagnosisMarker.builder()
                        .diagnosis(diagnosis)
                        .name(name)
                        .code(code)
//                        .description(description)
                        .species(species)
                        .type(MarkerType.LABORATORY)
                        .referenceUnit(unit)
                        .referenceMinimum(minRef)
                        .referenceMaximum(maxRef)
                        .build()
        );
    }

    private void savePrescriptionMarker(Diagnosis diagnosis, String name, String description, Species species) {
        standardMarkerRepository.save(
                StandardizeDiagnosisMarker.builder()
                        .diagnosis(diagnosis)
                        .name(name)
                        .description(description)
                        .species(species)
                        .type(MarkerType.PRESCRIPTION)
                        .build()
        );
    }
}
