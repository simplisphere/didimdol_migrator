package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.mssql.migrators.*;
import com.simplisphere.didimdolstandardize.postgresql.MarkerType;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
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
import org.springframework.scheduling.annotation.Async;
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
    private final LaboratoryMigrator laboratoryMigrator;
    private final LaboratoryService laboratoryService;

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
        Migrator self = (Migrator) applicationContext.getBean("gogangMigrator");

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
//            CompletableFuture<Void> prescriptionFuture = CompletableFuture.completedFuture(null);
            log.info("Vital 마이그레이션");
            CompletableFuture<Void> vitalFuture = self.migrateVital(hospital);
//            CompletableFuture<Void> vitalFuture = CompletableFuture.completedFuture(null);
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
        medicineService.findOrCreate(Medicine.builder().description("표준 크레메진").name("크레메진 세립").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 노바스크 정").name("노바스크 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 노바트 주사").name("노바트 주사").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 세레니아 정").name("세레니아 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레나젤 정").name("레나젤 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레메론 정").name("레메론 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 FUCO K").name("FUCO K 캡슐").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 베나실 정").name("베나실 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 레나메진 정").name("레나메진 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 텔미사탄 정").name("텔미사탄 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 인슐린 경구용").name("인슐린 경구용").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 인슐린 주사").name("인슐린 주사").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 아스피린 정").name("아스피린 정").build());
        medicineService.findOrCreate(Medicine.builder().description("표준 아스피린프로텍트 정").name("아스피린프로텍트 정").build());
    }

    // 표준화 Medicine 룰 생성
    private void prepareStandardizationMedicationRule(Hospital hospital) {

        List<StandardizedRule> rules = new ArrayList<>();

        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 1").description("고강 약품 맵핑 1").type(RuleType.PRESCRIPTION).fromName("크레메진").toName("크레메진 세립").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 2").description("고강 약품 맵핑 2").type(RuleType.PRESCRIPTION).fromName("노바스크정(암로디핀) 10mg 5mg").toName("노바스크 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 3").description("고강 약품 맵핑 3").type(RuleType.PRESCRIPTION).fromName("노바트주(세레니아)").toName("노바트 주사").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 4").description("고강 약품 맵핑 4").type(RuleType.PRESCRIPTION).fromName("세레니아(Cerenia) 정 24mg").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 5").description("고강 약품 맵핑 5").type(RuleType.PRESCRIPTION).fromName("레나젤정 800").toName("레나젤 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 6").description("고강 약품 맵핑 6").type(RuleType.PRESCRIPTION).fromName("레메론정 15mg").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 7").description("고강 약품 맵핑 7").type(RuleType.PRESCRIPTION).fromName("치료보조제 - FUCO K 후코케이 300mg 30캡슐").toName("FUCO K 캡슐").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 8").description("고강 약품 맵핑 8").type(RuleType.PRESCRIPTION).fromName("베나실정 10mg").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 9").description("고강 약품 맵핑 9").type(RuleType.PRESCRIPTION).fromName("레나메진").toName("레나메진 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 10").description("고강 약품 맵핑 10").type(RuleType.PRESCRIPTION).fromName("텔미사탄 40mg").toName("텔미사탄 정").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 약품 맵핑 11").description("고강 약품 맵핑 11").type(RuleType.PRESCRIPTION).fromName("*동종요법-Insulinum 200C").toName("인슐린 경구용").hospital(hospital).created(LocalDateTime.now()).build());
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
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100114", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100206", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100503", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100803", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101102", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101202", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101302", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA201501", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA203309", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA220305", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000114", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000208", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA777705", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000308", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00306", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00206", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00404", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00607", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00712", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00108", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3116", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3202", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3407", "BUN", Species.FELINE, "mg/dL", 10F, 30F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100114", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100206", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100503", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100803", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101102", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101202", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101302", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA201501", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA203309", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA220305", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000114", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000208", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA777705", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000308", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00306", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00206", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00404", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00607", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00712", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00108", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3116", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3202", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3407", "BUN", Species.CANINE, "mg/dL", 8F, 31F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100113", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100205", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100502", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101004", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101103", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101203", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101303", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA201601", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA203308", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA700101", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB3033103", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000113", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000209", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR304501", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000309", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00207", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00406", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00610", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00711", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00113", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3115", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3204", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3408", "CRE", Species.FELINE, "mg/dL", 0.8F, 1.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100113", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100205", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100502", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101004", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101103", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101203", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA101303", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA201601", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA203308", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA700101", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB3033103", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000113", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000209", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR304501", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA2000309", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00207", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00406", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00610", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00711", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00113", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3115", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3204", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "v3408", "CRE", Species.CANINE, "mg/dL", 0.8F, 1.4F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR7088801", "SDMA", Species.FELINE, "mg/dL", 0F, 14F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR7088801", "SDMA", Species.CANINE, "mg/dL", 0F, 15F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB303311", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR304503", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB100105", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB100404", "UPC", Species.FELINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB303311", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FR304503", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB100105", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB100404", "UPC", Species.CANINE, "mg/dL", 0F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA202601", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB400102", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB40010", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA800803", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000122", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-105", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3206", "K", Species.FELINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA202601", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB400102", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FB40010", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA800803", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000122", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-105", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "V3206", "K", Species.CANINE, "mEq/L", 3.5F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100116", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100411", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100509", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000116", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA800806", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-103", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00408", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00613", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00714", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00120", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-120", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-121", "P", Species.FELINE, "mg/dL", 3F, 5.5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100116", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100411", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA100509", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA1000116", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA800806", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-103", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00408", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00613", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00714", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "PT10_00120", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-120", "P", Species.CANINE, "mg/dL", 2.5F, 5F));
        markers.add(generateLaboratoryMarker(diagnosis1, "FA8008-121", "P", Species.CANINE, "mg/dL", 2.5F, 5F));

//        // 당뇨 질환 혈액 검사 표준화 마커
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100504", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100603", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100701", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100902", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA203301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101002", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101104", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201101", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB200505", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA220306", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA1000102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA2000201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA8008-109", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA2000301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00309", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00209", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00407", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00612", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00701", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00116", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "v3102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "V3209", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "v3412", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100504", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100603", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100701", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA100902", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA203301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101002", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101104", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA101301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201101", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB200505", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA220306", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA1000102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA2000201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA8008-109", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA2000301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00309", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00209", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00407", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00612", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00701", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "PT10_00116", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "v3102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "V3209", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "v3412", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FR3333301", "FRUC", Species.FELINE, "μmol/L", 150F, 300F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FR3333301", "FRUC", Species.CANINE, "μmol/L", 120F, 250F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201301", "HbA1c", Species.FELINE, "%", 4F, 6F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FA201301", "HbA1c", Species.CANINE, "%", 4F, 6F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB100107", "U.GLU", Species.FELINE, "", 0F, 9999F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB100410", "U.GLU", Species.FELINE, "", 0F, 9999F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB100107", "U.GLU", Species.CANINE, "", 0F, 9999F));
        markers.add(generateLaboratoryMarker(diagnosis2, "FB100410", "U.GLU", Species.CANINE, "", 0F, 9999F));

        // 비만 질환 혈액 검사 표준화 마커
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100504", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100603", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100701", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100902", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA203301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101002", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101104", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201101", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FB200505", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA220306", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA1000102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000201", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA8008-109", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000301", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00309", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00209", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00407", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00612", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00701", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00116", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3102", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "V3209", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3412", "GLU", Species.FELINE, "mg/dL", 0F, 150F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100504", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100603", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100701", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100902", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA203301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101002", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101104", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201101", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FB200505", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA220306", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA1000102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000201", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA8008-109", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000301", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00309", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00209", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00407", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00612", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00701", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00116", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3102", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "V3209", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3412", "GLU", Species.CANINE, "mg/dL", 0F, 140F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100108", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100204", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100306", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101005", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101108", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101208", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA200201", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "혈청검사01", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA203307", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA1000108", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000207", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000306", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00304", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00204", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00604", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00707", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00104", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3108", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3403", "ALT", Species.FELINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100108", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100204", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100306", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101005", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101108", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101208", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA200201", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "혈청검사01", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA203307", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA1000108", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000207", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA2000306", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00304", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00204", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00604", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00707", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00104", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3108", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3403", "ALT", Species.CANINE, "U/L", 10F, 45F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100305", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100402", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101109", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101207", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA200101", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00305", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00106", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3109", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3404", "AST", Species.FELINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100305", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100402", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101109", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA101207", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA200101", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00305", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "PT10_00106", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3109", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "v3404", "AST", Species.CANINE, "U/L", 10F, 40F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100111", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100405", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100602", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100802", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA202001", "T.Chol", Species.FELINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100111", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100405", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100602", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA100802", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA202001", "T.Chol", Species.CANINE, "mg/dL", 100F, 200F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201701", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FB3033101", "U.Acid", Species.FELINE, "mg/dL", 2.5F, 7F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FA201701", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F));
        markers.add(generateLaboratoryMarker(diagnosis3, "FB3033101", "U.Acid", Species.CANINE, "mg/dL", 2.5F, 6.5F));

        // 심장 질환 혈액 검사 표준화 마커
        // Troponin 없음
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH116", "Troponin I ", Species.FELINE, "ng/mL", 0F, 0.16F));
//        markers.add(generateLaboratoryMarker(diagnosis4, "CH116", "Troponin I", Species.CANINE, "ng/mL", 0F, 0.1F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FA100403", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FA200501", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "PT10_00111", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "v3122", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "v3410", "CK-MB", Species.FELINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FA100403", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FA200501", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "PT10_00111", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "v3122", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "v3410", "CK-MB", Species.CANINE, "UI/l", 4.9F, 6.3F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR33301", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR330301", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FK3301", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FK3401", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR330302", "NT-proBNP", Species.FELINE, "pmol/L", 0F, 100F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR33301", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR330301", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FK3301", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FK3401", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));
        markers.add(generateLaboratoryMarker(diagnosis4, "FR330302", "NT-proBNP", Species.CANINE, "pmol/L", 0F, 900F));

        // 신장 질환 처방 표준화 마커
        List<String> diagnosis1StandardMarkers = List.of("크레메진 세립", "노바스크 정", "노바트 주사", "세레니아 정", "레나젤 정", "레메론 정", "FUCO K 캡슐", "베나실 정", "레나메진 정", "텔미사탄 정");
        diagnosis1StandardMarkers.forEach(medicineName -> {
            markers.add(generatePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.FELINE));
            markers.add(generatePrescriptionMarker(diagnosis1, medicineName, medicineName + " 처방", Species.CANINE));
        });


        // 당뇨 질환 처방 표준화 마커
        List<String> diagnosis2StandardMarkers = List.of("인슐린 주사", "인슐린 경구용");
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

    @Async
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

    @Async
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

    @Async
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

    @Async
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

    @Async
    @Override
    public CompletableFuture<Void> migrateLaboratory(Hospital hospital) {
        log.info("Laboratory migration started");
        return migrateLaboratoryType(hospital)
                .thenCompose(result -> migrateLaboratoryItem(hospital))
                .thenCompose(result -> migrateLaboratoryReference(hospital))
                .thenCompose(result -> migrateLaboratoryResult(hospital));
    }

    @Async
    @Override
    public CompletableFuture<Void> migrateLaboratoryType(Hospital hospital) {
        log.info("Laboratory Type migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 2000, sort);
        int completed = 0;
        long totalElements;
        Page<LaboratoryType> newLaboratoryTypes;

        do {
            newLaboratoryTypes = laboratoryMigrator.convertLaboratoryType(hospital, pageRequest);
            laboratoryService.saveLaboratoryTypes(newLaboratoryTypes.getContent());
            completed += newLaboratoryTypes.getNumberOfElements();
            totalElements = newLaboratoryTypes.getTotalElements();
            log.debug("Laboratory Type 총 {} 중 {} 저장 완료", newLaboratoryTypes.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (newLaboratoryTypes.hasNext());

        log.info("migrated laboratory type count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> migrateLaboratoryItem(Hospital hospital) {
        log.info("Laboratory Item migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 2000, sort);
        int completed = 0;
        long totalElements;
        Page<LaboratoryItem> newLaboratoryItems;

        do {
            newLaboratoryItems = laboratoryMigrator.convertLabItem(hospital, pageRequest);
            laboratoryService.saveLaboratoryItems(newLaboratoryItems.getContent());
            completed += newLaboratoryItems.getNumberOfElements();
            totalElements = newLaboratoryItems.getTotalElements();
            log.debug("Laboratory Item 총 {} 중 {} 저장 완료", newLaboratoryItems.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (newLaboratoryItems.hasNext());

        log.info("migrated laboratory item count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> migrateLaboratoryReference(Hospital hospital) {
        log.info("Laboratory Reference migration started");
        Sort sort = Sort.by(Sort.Order.asc("id"));
        PageRequest pageRequest = PageRequest.of(0, 2000, sort);
        int completed = 0;
        long totalElements;
        Page<LaboratoryReference> newLaboratoryRefs;

        do {
            newLaboratoryRefs = laboratoryMigrator.convertLabReference(hospital, pageRequest);
            laboratoryService.saveLaboratoryReferences(newLaboratoryRefs.getContent());
            completed += newLaboratoryRefs.getNumberOfElements();
            totalElements = newLaboratoryRefs.getTotalElements();
            log.debug("Laboratory Reference 총 {} 중 {} 저장 완료", newLaboratoryRefs.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (newLaboratoryRefs.hasNext());

        log.info("migrated laboratory reference count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> migrateLaboratoryResult(Hospital hospital) {
        log.info("Laboratory Result migration started");
        PageRequest pageRequest = PageRequest.of(0, 10000);
//        PageRequest pageRequest = PageRequest.of(0, 10);
        int completed = 0;
        long totalElements;
        Page<LaboratoryResult> newLaboratoryResults;

        do {
            newLaboratoryResults = laboratoryMigrator.convertLabResult(hospital, pageRequest);
            laboratoryService.saveLaboratoryResults(newLaboratoryResults.getContent());
            completed += newLaboratoryResults.getNumberOfElements();
            totalElements = newLaboratoryResults.getTotalElements();
            log.debug("Laboratory Result 총 {} 중 {} 저장 완료", newLaboratoryResults.getTotalElements(), completed);
            pageRequest = pageRequest.next();
        } while (newLaboratoryResults.hasNext());

        log.info("migrated laboratory result count: {}", totalElements);
        return CompletableFuture.completedFuture(null);
    }

    @Async
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

    @Async
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
