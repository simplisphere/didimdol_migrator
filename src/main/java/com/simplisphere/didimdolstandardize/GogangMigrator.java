package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.mssql.migrators.*;
import com.simplisphere.didimdolstandardize.postgresql.MarkerType;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.AssessmentRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import com.simplisphere.didimdolstandardize.postgresql.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class GogangMigrator implements Migrator {

    private final ApplicationContext applicationContext;

    private final PatientMigrator patientMigrator;
    private final DiagnosisMigrator diagnosisMigrator;
    private final AssessmentMigrator assessmentMigrator;

    private final DiagnosisService diagnosisService;
    private final PatientRepository patientRepository;
    private final HospitalRepository targetHospitalRepository;
    private final RuleService ruleService;
    private final HospitalDiagnosisService hospitalDiagnosisService;
    private final AssessmentRepository assessmentRepository;
    private final ChartMigrator chartMigrator;
    private final ChartService chartService;
    private final VitalMigrator vitalMigrator;
    private final VitalService vitalService;
    private final MedicineService medicineService;
    private final StandardMarkerService standardMarkerService;
    private final PrescriptionMigrator prescriptionMigrator;
    private final PrescriptionService prescriptionService;

    private Hospital hospital;

    @Override
    public void beforeMigrate() {
        log.info("병원 데이터 생성");
        hospital = preparedHospital();

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
    }

    @Override
    public void migrate() {
        Migrator self = applicationContext.getBean(Migrator.class);

        log.info("환자 데이터 표준화");
        CompletableFuture<Void> patientFuture = self.migratePatient(hospital);

        log.info("차트 데이터 표준화");
        CompletableFuture<Void> chartFuture = patientFuture.thenCompose(result -> self.migrateChart(hospital));

        CompletableFuture<Void> assessmentAndDiagnosisFuture = chartFuture.thenCompose(result -> {
            log.info("Assessment 마이그레이션");
            CompletableFuture<Void> assessmentFuture = self.migrateAssessment();
            log.info("Diagnosis 마이그레이션");
            CompletableFuture<Void> diagnosisFuture = self.migrateDiagnosis(hospital);
            return CompletableFuture.allOf(assessmentFuture, diagnosisFuture);
        });

        CompletableFuture<Void> finalFuture = assessmentAndDiagnosisFuture.thenCompose(result -> {
            log.info("Laboratory Examination 마이그레이션");
            CompletableFuture<Void> laboratoryFuture = self.migrateLaboratory(hospital);
            log.info("Prescription 마이그레이션");
            CompletableFuture<Void> prescriptionFuture = self.migratePrescription(hospital);
            log.info("Vital 마이그레이션");
            CompletableFuture<Void> vitalFuture = self.migrateVital(hospital);
            return CompletableFuture.allOf(laboratoryFuture, prescriptionFuture, vitalFuture);
        });

        finalFuture.join();
    }

    @Override
    public void afterMigrate() {

    }

    private Hospital preparedHospital() {
        return targetHospitalRepository.findByName("고강").orElseGet(() -> targetHospitalRepository.save(Hospital.builder()
                .name("고강")
                .address("경기도 부천시")
                .phone("032-123-4567")
                .build()
        ));
    }

    // 표준 진단 데이터 생성
    private void prepareDiagnosis() {
        diagnosisService.findOrCreate(Diagnosis.builder().code("A1001").description("표준화 신장 질환").name("신장 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisService.findOrCreate(Diagnosis.builder().code("B1001").description("표준화 당뇨 질환").name("당뇨 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisService.findOrCreate(Diagnosis.builder().code("C1001").description("표준화 비만 질환").name("비만 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
        diagnosisService.findOrCreate(Diagnosis.builder().code("D1001").description("표준화 심장 질환").name("심장 질환").created(LocalDateTime.now()).updated(LocalDateTime.now()).build());
    }

    // 표준화 Diagnosis 룰 생성
    private void prepareStandardizationDiagnosisRule(Hospital hospital) {

        List<StandardizedRule> rules = new ArrayList<>();

        // 신장 질환
        rules.add(StandardizedRule.builder().name("고강 신장 질환 1").description("고강 신장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 2").description("고강 신장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("신부전").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 3").description("고강 신장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("급성 신부전").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 4").description("고강 신장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("신우신장염").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 5").description("고강 신장 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("신부전(IRIS 2)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 6").description("고강 신장 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("신부전(IRIS 3)").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 7").description("고강 신장 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("만성 신부전").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 8").description("고강 신장 질환 표준화 룰 8").type(RuleType.DIAGNOSIS).fromName("신장낭종").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 9").description("고강 신장 질환 표준화 룰 9").type(RuleType.DIAGNOSIS).fromName("우측신장무형성증").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 10").description("고강 신장 질환 표준화 룰 10").type(RuleType.DIAGNOSIS).fromName("간신장 증후군").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 11").description("고강 신장 질환 표준화 룰 11").type(RuleType.DIAGNOSIS).fromName("선천성 및 발달성 신장 질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 12").description("고강 신장 질환 표준화 룰 12").type(RuleType.DIAGNOSIS).fromName("신장질환-선천성 및 발달성").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 13").description("고강 신장 질환 표준화 룰 13").type(RuleType.DIAGNOSIS).fromName("신장 신생물").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 14").description("고강 신장 질환 표준화 룰 14").type(RuleType.DIAGNOSIS).fromName("만성 신장질환에 의한 빈혈").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 15").description("고강 신장 질환 표준화 룰 15").type(RuleType.DIAGNOSIS).fromName("신장주변 가성낭포").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 16").description("고강 신장 질환 표준화 룰 16").type(RuleType.DIAGNOSIS).fromName("다낭신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 17").description("고강 신장 질환 표준화 룰 17").type(RuleType.DIAGNOSIS).fromName("신장비대").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 18").description("고강 신장 질환 표준화 룰 18").type(RuleType.DIAGNOSIS).fromName("원발성 신장질환").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 당뇨 질환
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 1").description("고강 당뇨 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("당뇨").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 2").description("고강 당뇨 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("당뇨병-케톤산증").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 3").description("고강 당뇨 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("당뇨병-비합병증성").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 4").description("고강 당뇨 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("당뇨병-비합병증성 - 고양이").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 5").description("고강 당뇨 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("당뇨병-케톤성").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 6").description("고강 당뇨 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("당뇨병-고삼투성 비케톤성 증후근").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 7").description("고강 당뇨 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("당뇨병-고삼투압성 혼수").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 8").description("고강 당뇨 질환 표준화 룰 8").type(RuleType.DIAGNOSIS).fromName("당뇨성 간장애").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 9").description("고강 당뇨 질환 표준화 룰 9").type(RuleType.DIAGNOSIS).fromName("신성 당뇨").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 비만 질환
        rules.add(StandardizedRule.builder().name("고강 비만 질환 1").description("고강 비만 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("비만").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 비만 질환 2").description("고강 비만 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("비만 -중증").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 비만 질환 3").description("고강 비만 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("비만 - bcs 5/5").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 심장 질환
        rules.add(StandardizedRule.builder().name("고강 심장 질환 1").description("고강 심장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("심장질환").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 2").description("고강 심장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("심비대").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 3").description("고강 심장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 4").description("고강 심장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("심잡음").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 5").description("고강 심장 질환 표준화 룰 5").type(RuleType.DIAGNOSIS).fromName("울혈성 우심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 6").description("고강 심장 질환 표준화 룰 6").type(RuleType.DIAGNOSIS).fromName("울혈성 좌심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 7").description("고강 심장 질환 표준화 룰 7").type(RuleType.DIAGNOSIS).fromName("확장성 심근병; DCM - 개").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 8").description("고강 심장 질환 표준화 룰 8").type(RuleType.DIAGNOSIS).fromName("심실중격결손").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 9").description("고강 심장 질환 표준화 룰 9").type(RuleType.DIAGNOSIS).fromName("심낭 삼출액").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 10").description("고강 심장 질환 표준화 룰 10").type(RuleType.DIAGNOSIS).fromName("심근병증-복서").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 11").description("고강 심장 질환 표준화 룰 11").type(RuleType.DIAGNOSIS).fromName("심근병-비대").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 12").description("고강 심장 질환 표준화 룰 12").type(RuleType.DIAGNOSIS).fromName("비대성 심근병;HCM - 고양이").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 13").description("고강 심장 질환 표준화 룰 13").type(RuleType.DIAGNOSIS).fromName("심근병-심근 제한성").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 14").description("고강 심장 질환 표준화 룰 14").type(RuleType.DIAGNOSIS).fromName("제한성 심근병; RCM -고양이").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 15").description("고강 심장 질환 표준화 룰 15").type(RuleType.DIAGNOSIS).fromName("심근병-복서").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 18").description("고강 심장 질환 표준화 룰 18").type(RuleType.DIAGNOSIS).fromName("심내막심근질환").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 19").description("고강 심장 질환 표준화 룰 19").type(RuleType.DIAGNOSIS).fromName("심근경색").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 20").description("고강 심장 질환 표준화 룰 20").type(RuleType.DIAGNOSIS).fromName("심근염").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 21").description("고강 심장 질환 표준화 룰 21").type(RuleType.DIAGNOSIS).fromName("창상성 심근염").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 22").description("고강 심장 질환 표준화 룰 22").type(RuleType.DIAGNOSIS).fromName("심근 종양").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 23").description("고강 심장 질환 표준화 룰 23").type(RuleType.DIAGNOSIS).fromName("울혈성우심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 24").description("고강 심장 질환 표준화 룰 24").type(RuleType.DIAGNOSIS).fromName("울혈성좌심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 25").description("고강 심장 질환 표준화 룰 25").type(RuleType.DIAGNOSIS).fromName("심근병-비대성").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 26").description("고강 심장 질환 표준화 룰 26").type(RuleType.DIAGNOSIS).fromName("확장성 심근병").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());

        ruleService.saveAll(rules);
    }

    // 표준 약품 데이터 생성
    private void prepareMedicine() {
        medicineService.findOrCreate(Medicine.builder().description("표준 크레메진").name("크레메진").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 노바스크 정").name("노바스크 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 노바트 주사").name("노바트 주사").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 세레니아 정").name("세레니아 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레나젤 정").name("레나젤 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레메론 정").name("레메론 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 FUCO K").name("FUCO K").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 베나실 정").name("베나실 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레나메진 정").name("레나메진 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 텔미사탄").name("텔미사탄").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 인슐린 주사").name("인슐린 주사").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 아스피린 정").name("아스피린 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 아스피린프로텍트 정").name("아스피린프로텍트 정").build());
    }

    // 표준화 Medicine 룰 생성
    private void prepareStandardizationMedicationRule(Hospital hospital) {

        List<StandardizedRule> rules = new ArrayList<>();

        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 1").description("고강 약품 맵핑 1").type(RuleType.PRESCRIPTION).fromName("크레메진").toName("크레메진").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 2").description("고강 약품 맵핑 2").type(RuleType.PRESCRIPTION).fromName("노바스크정(암로디핀) 10mg 5mg").toName("노바스크 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 3").description("고강 약품 맵핑 3").type(RuleType.PRESCRIPTION).fromName("노바트주(세레니아)").toName("노바트 주사").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 4").description("고강 약품 맵핑 4").type(RuleType.PRESCRIPTION).fromName("세레니아(Cerenia) 정 24mg").toName("세레니아 캡슐").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 5").description("고강 약품 맵핑 5").type(RuleType.PRESCRIPTION).fromName("레나젤정 800").toName("레나젤 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 6").description("고강 약품 맵핑 6").type(RuleType.PRESCRIPTION).fromName("레메론정 15mg").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 7").description("고강 약품 맵핑 7").type(RuleType.PRESCRIPTION).fromName("치료보조제 - FUCO K 후코케이 300mg 30캡슐").toName("FUCO K").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 8").description("고강 약품 맵핑 8").type(RuleType.PRESCRIPTION).fromName("베나실정 10mg").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 9").description("고강 약품 맵핑 9").type(RuleType.PRESCRIPTION).fromName("레나메진").toName("레나메진 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 10").description("고강 약품 맵핑 10").type(RuleType.PRESCRIPTION).fromName("텔미사탄 40mg").toName("텔미사탄").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 11").description("고강 약품 맵핑 11").type(RuleType.PRESCRIPTION).fromName("*동종요법-Insulinum 200C").toName("인슐린 주사").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 12").description("고강 약품 맵핑 12").type(RuleType.PRESCRIPTION).fromName("당뇨주사").toName("인슐린 주사").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 13").description("고강 약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("아스피린").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 13").description("고강 약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("아스피린성인용").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 13").description("고강 약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("아스피린소아용").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 13").description("고강 약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("아스피린프로텍트").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());

        ruleService.saveAll(rules);
    }

    // 표준화 마커 생성
    private void prepareStandardizedMarker() {

        Diagnosis diagnosis1 = diagnosisService.findByName("신장 질환").orElseThrow(() -> new RuntimeException("Diagnosis(신장 질환) not found"));
        Diagnosis diagnosis2 = diagnosisService.findByName("당뇨 질환").orElseThrow(() -> new RuntimeException("Diagnosis(당뇨 질환) not found"));
        Diagnosis diagnosis3 = diagnosisService.findByName("비만 질환").orElseThrow(() -> new RuntimeException("Diagnosis(비만 질환) not found"));
        Diagnosis diagnosis4 = diagnosisService.findByName("심장 질환").orElseThrow(() -> new RuntimeException("Diagnosis(심장 질환) not found"));

        List<StandardizeDiagnosisMarker> markers = new ArrayList<>();

//        // 신장 질환 혈액 검사 표준화 마커
//        markers.add(generateLaboratoryMarker(diagnosis1, "GA009", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH011", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0056", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0014", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0113", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "PT0010", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "GA009", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH011", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0056", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0014", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0113", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "PT0010", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH076", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH019", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0063", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0004", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0116", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "PT0018", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH076", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH019", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0063", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0004", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0116", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "PT0018", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH143", "SDMA", Species.FELINE, "mg/dL", 0F, 14F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH143", "SDMA", Species.CANINE, "mg/dL", 0F, 15F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "UR021", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0098", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH063", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "UR021", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "LB0098", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH063", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH044", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "EL002", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "K+", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "GA018", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "ISM002", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "CH044", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "EL002", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "K+", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "GA018", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "ISM002", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0007", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
//        markers.add(generateLaboratoryMarker(diagnosis1, "BS0007", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
//
//        // 당뇨 질환 혈액 검사 표준화 마커
//        markers.add(generateLaboratoryMarker(diagnosis2, "GA013", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "UR012", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH077", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH026", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0071", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "BS0015", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0117", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0151", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "PT0005", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "UR003", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "혈당(glucose, glu)", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "GA013", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "UR012", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH077", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH026", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0071", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "BS0015", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0117", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "LB0151", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "PT0005", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "UR003", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "혈당(glucose, glu)", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH023", "FRUC", Species.FELINE, "μmol/L", 150F, 300F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "SO0003", "FRUC", Species.FELINE, "μmol/L", 150F, 300F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "CH023", "FRUC", Species.CANINE, "μmol/L", 120F, 250F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "SO0003", "FRUC", Species.CANINE, "μmol/L", 120F, 250F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "PT0025", "HbA1c", Species.FELINE, "%", 4F, 6F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "c0001", "HbA1c", Species.FELINE, "%", 4F, 6F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "PT0025", "HbA1c", Species.CANINE, "%", 4F, 6F));
//        markers.add(generateLaboratoryMarker(diagnosis2, "c0001", "HbA1c", Species.CANINE, "%", 4F, 6F));
//        // U.GLU 검사 항목이 없음
////        markers.add(generateLaboratoryMarker(diagnosis2, "", "U.GLU", Species.FELINE, "", 0F, 9999F));
////        markers.add(generateLaboratoryMarker(diagnosis2, "", "U.GLU", Species.CANINE, "", 0F, 9999F));
//
//        // 비만 질환 혈액 검사 표준화 마커
//        markers.add(generateLaboratoryMarker(diagnosis3, "GA013", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "UR012", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH077", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH026", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0071", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0015", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0117", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0151", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0005", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "UR003", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "혈당(glucose, glu)", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "GA013", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "UR012", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH077", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH026", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0071", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0015", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0117", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0151", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0005", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "UR003", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH105", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH003", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0051", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0012", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH054", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0001", "ALT", Species.FELINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH105", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH003", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0051", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0012", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH054", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0001", "ALT", Species.CANINE, "U/L", 10F, 45F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH106", "AST", Species.FELINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH007", "AST", Species.FELINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0054", "AST", Species.FELINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0008", "AST", Species.FELINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0002", "AST", Species.FELINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH106", "AST", Species.CANINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH007", "AST", Species.CANINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0054", "AST", Species.CANINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0008", "AST", Species.CANINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0002", "AST", Species.CANINE, "U/L", 10F, 40F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0092", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0005", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0016", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0092", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "BS0005", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0016", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH042", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0096", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0023", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "CH042", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "LB0096", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F));
//        markers.add(generateLaboratoryMarker(diagnosis3, "PT0023", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F));
//
//        // 심장 질환 혈액 검사 표준화 마커
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH116", "Troponin I ", Species.FELINE, "ng/mL", 0F, 0.16F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH116", "Troponin I", Species.CANINE, "ng/mL", 0F, 0.1F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH018", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH018", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH129", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "fProBNP", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH129", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "fProBNP", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));

        // 신장 질환 처방 표준화 마커
        List<String> diagnosis1StandardMarkers = List.of("크레메진", "노바스크 정", "노바트 주사", "세레니아 정", "레나젤 정", "레메론 정", "FUCO K", "베나실 정", "레나메진 정", "텔미사탄");
        diagnosis1StandardMarkers.forEach(medicineName -> {
            markers.add(generatePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.FELINE));
            markers.add(generatePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.CANINE));
        });


        // 당뇨 질환 처방 표준화 마커
        List<String> diagnosis2StandardMarkers = List.of("인슐린 주사");
        diagnosis2StandardMarkers.forEach(medicineName -> {
            markers.add(generatePrescriptionMarker(diagnosis2, medicineName, medicineName + " 처방", Species.FELINE));
            markers.add(generatePrescriptionMarker(diagnosis2, medicineName, medicineName + " 처방", Species.CANINE));
        });

        // 비만 질환 처방 표준화 마커
//        List<String> diagnosis3StandardMarkers = List.of("세레니아 정", "레메론 정");
//        diagnosis3StandardMarkers.forEach(medicineName -> {
//            markers.add(generatePrescriptionMarker(diagnosis3, medicineName, medicineName + " 처방", Species.FELINE));
//            markers.add(generatePrescriptionMarker(diagnosis3, medicineName, medicineName + " 처방", Species.CANINE));
//        });

        // 심장 질환 처방 표준화 마커
        List<String> diagnosis4StandardMarkers = List.of("베나실 정", "아스피린 정", "아스피린프로텍트 정");
        diagnosis4StandardMarkers.forEach(medicineName -> {
            markers.add(generatePrescriptionMarker(diagnosis4, medicineName, medicineName + " 처방", Species.FELINE));
            markers.add(generatePrescriptionMarker(diagnosis4, medicineName, medicineName + " 처방", Species.CANINE));
        });

        standardMarkerService.saveAll(markers);
    }

    @Override
    public CompletableFuture<Void> migratePatient(Hospital hospital) {
        Sort sort = Sort.by("ptid").ascending();
        PageRequest pageRequest = PageRequest.of(0, 1000, sort);
        int completed = 0;
        long totalElements;
        Page<Patient> patients;

        do {
            patients = patientMigrator.convertPatient(hospital, pageRequest);
            patientRepository.saveAll(patients.getContent());
            completed += patients.getNumberOfElements();
            totalElements = patients.getTotalElements();
            log.debug("patient 총 {} 중 {} 저장 완료", totalElements, completed);
            pageRequest = pageRequest.next();
        } while (patients.hasNext());

        log.info("migrated patient count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateChart(Hospital hospital) {
        Sort sort = Sort.by(Sort.Order.asc("id")).and(Sort.by(Sort.Order.asc("listOrder")));
        PageRequest pageRequest = PageRequest.of(0, 5000, sort);
        int completed = 0;
        long totalElements;
        Page<Chart> charts;

        do {
            log.debug(pageRequest.toString());
            charts = chartMigrator.convertChart(hospital, pageRequest);
            chartService.saveAll(charts.getContent());
            log.debug("total : {}", charts.getTotalElements());
            completed += charts.getNumberOfElements();
            totalElements = charts.getTotalElements();
            log.debug("chart 총 {} 중 {} 저장 완료", totalElements, completed);
            log.debug("pagerequest hasNext : {}", charts.hasNext());
            pageRequest = pageRequest.next();
        } while (charts.hasNext());

        log.info("migrated chart count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateAssessment() {
        log.info("Assessment migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 2000, sort);
        int completed = 0;
        long totalElements;
        Page<Assessment> assessments;

        do {
            assessments = assessmentMigrator.convertAssessment(hospital, pageRequest);
            assessmentRepository.saveAll(assessments.getContent());
            completed += assessments.getNumberOfElements();
            totalElements = assessments.getTotalElements();
            log.debug("Assessment 총 {} 중 {} 저장 완료", assessments.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (assessments.hasNext());

        log.info("migrated assessments count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateDiagnosis(Hospital hospital) {
        log.info("Diagnosis migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 2000, sort);
        int completed = 0;
        long totalElements;
        Page<HospitalDiagnosis> hospitalDiagnoses;

        do {
            hospitalDiagnoses = diagnosisMigrator.convertDiagnosis(hospital, pageRequest);
            hospitalDiagnosisService.saveAll(hospitalDiagnoses.getContent());
            completed += hospitalDiagnoses.getNumberOfElements();
            totalElements = hospitalDiagnoses.getTotalElements();
            log.debug("Diagnosis 총 {} 중 {} 저장 완료", hospitalDiagnoses.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (hospitalDiagnoses.hasNext());

        log.info("migrated hospitalDiagnosis count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateLaboratory(Hospital hospital) {
        log.info("Laboratory migration started");
        return migrateLaboratoryType(hospital)
                .thenCompose(result -> migrateLaboratoryItem(hospital))
                .thenCompose(result -> migrateLaboratoryReference(hospital))
                .thenCompose(result -> migrateLaboratoryResult(hospital));
    }

    @Override
    public CompletableFuture<Void> migrateLaboratoryType(Hospital hospital) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateLaboratoryItem(Hospital hospital) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateLaboratoryReference(Hospital hospital) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateLaboratoryResult(Hospital hospital) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migratePrescription(Hospital hospital) {
        log.info("Prescription migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 5000, sort);
        int completed = 0;
        long totalElements;
        Page<Prescription> prescriptions;

        do {
            prescriptions = prescriptionMigrator.convertPrescription(hospital, pageRequest);
            prescriptionService.saveAll(prescriptions.getContent());
            completed += prescriptions.getNumberOfElements();
            totalElements = prescriptions.getTotalElements();
            log.debug("Prescription 총 {} 중 {} 저장 완료", prescriptions.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (prescriptions.hasNext());

        log.info("migrated prescription count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> migrateVital(Hospital hospital) {
        log.info("Vital migration started");
//        Sort sort = Sort.by(Sort.Order.desc("createdAt")).and(Sort.by(Sort.Order.asc("id")));
        PageRequest pageRequest = PageRequest.of(0, 5000);
        int completed = 0;
        long totalElements;
        Page<Vital> newVitals;

        do {
            newVitals = vitalMigrator.convertVital(hospital, pageRequest);
            vitalService.saveAll(newVitals.getContent());
            completed += newVitals.getNumberOfElements();
            totalElements = newVitals.getTotalElements();
            log.debug("Vital 총 {} 중 {} 저장 완료", newVitals.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (newVitals.hasNext());

        log.info("migrated vital count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    private StandardizeDiagnosisMarker generateLaboratoryMarker(Diagnosis diagnosis, String code, String name, Species species, String unit, Float minRef, Float maxRef) {
        return StandardizeDiagnosisMarker.builder()
                .diagnosis(diagnosis)
                .name(name)
                .code(code)
//                        .description(description)
                .species(species)
                .type(MarkerType.LABORATORY)
                .referenceUnit(unit)
                .referenceMinimum(minRef)
                .referenceMaximum(maxRef)
                .build();
    }

    private StandardizeDiagnosisMarker generatePrescriptionMarker(Diagnosis diagnosis, String name, String description, Species species) {
        return StandardizeDiagnosisMarker.builder()
                .diagnosis(diagnosis)
                .name(name)
                .description(description)
                .species(species)
                .type(MarkerType.PRESCRIPTION)
                .build();
    }
}
