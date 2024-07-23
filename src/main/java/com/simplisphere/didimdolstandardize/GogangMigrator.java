package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.mssql.migrators.*;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
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

    private Hospital hospital;

    @Override
    public void beforeMigrate() {
        log.info("병원 데이터 생성");
        hospital = preparedHospital();

        log.info("표준화 Diagnosis 생성");
        prepareDiagnosis();

        log.info("Diagnosis 표준화 룰 생성");
        prepareStandardizationDiagnosisRule(hospital);

//        log.info("표준화 약품 생성");
//        prepareMedicine();
//
//        log.info("약품 표준화 룰 생성");
//        prepareStandardizationMedicationRule(hospital);
//
//        log.info("표준화 마커 생성");
//        prepareStandardizedMarker();
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
        rules.add(StandardizedRule.builder().name("고강 신장 질환 1").description("신장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("신장결석").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 2").description("신장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("신우신장염").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 3").description("신장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("신장낭종").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 신장 질환 4").description("신장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("우측신장무형성증").toName("신장 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 당뇨 질환
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 1").description("당뇨 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("당뇨").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 2").description("당뇨 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("당뇨병-케톤산증").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 당뇨 질환 3").description("당뇨 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("당뇨병-비합병증성").toName("당뇨 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 비만 질환
        rules.add(StandardizedRule.builder().name("고강 비만 질환 1").description("비만 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("비만").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 비만 질환 2").description("비만 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("비만 -중증").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 비만 질환 2").description("비만 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("비만 - bcs 5/5").toName("비만 질환").hospital(hospital).created(LocalDateTime.now()).build());

        // 심장 질환
        rules.add(StandardizedRule.builder().name("고강 심장 질환 1").description("심장 질환 표준화 룰 1").type(RuleType.DIAGNOSIS).fromName("심장질환").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 2").description("심장 질환 표준화 룰 2").type(RuleType.DIAGNOSIS).fromName("심비대").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 3").description("심장 질환 표준화 룰 3").type(RuleType.DIAGNOSIS).fromName("심부전").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());
        rules.add(StandardizedRule.builder().name("고강 심장 질환 4").description("심장 질환 표준화 룰 4").type(RuleType.DIAGNOSIS).fromName("심잡음").toName("심장 질환").hospital(hospital).created(LocalDateTime.now()).build());

        ruleService.saveAll(rules);
    }

    // 표준 약품 데이터 생성
//    private void prepareMedicine() {
//        medicineRepository.save(Medicine.builder().description("표준 레메론 정").name("레메론 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 베나실 정").name("베나실 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 텔미원 정").name("텔미원 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 레나메진 캡슐").name("레나메진 캡슐").build());
//        medicineRepository.save(Medicine.builder().description("표준 세레니아 정").name("세레니아 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 암로디핀 정").name("암로디핀 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 텔미로탄 정").name("텔미로탄 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 아스피린 정").name("아스피린 정").build());
//        medicineRepository.save(Medicine.builder().description("표준 캐닌슐린 주사").name("캐닌슐린 주사").build());
//    }

    // 표준화 Medicine 룰 생성
//    private void prepareStandardizationMedicationRule(Hospital hospital) {
//
//        List<StandardizedRule> rules = new ArrayList<>();
//
//        rules.add(StandardizedRule.builder().name("약품 맵핑 1").description("약품 맵핑 1").type(RuleType.PRESCRIPTION).fromName("S_레메론_정_30T/box").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 2").description("약품 맵핑 2").type(RuleType.PRESCRIPTION).fromName("S_베나실_정_100T/box").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 3").description("약품 맵핑 3").type(RuleType.PRESCRIPTION).fromName("S_텔미원_정_30T_box").toName("텔미원 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 4").description("약품 맵핑 4").type(RuleType.PRESCRIPTION).fromName("S_레나메진_캡슐_7cap/90포/box").toName("레나메진 캡슐").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 5").description("약품 맵핑 5").type(RuleType.PRESCRIPTION).fromName("S_세레니아_정_4T/box").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 6").description("약품 맵핑 6").type(RuleType.PRESCRIPTION).fromName("레메론_정_15.0mg_30T").toName("레메론 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 7").description("약품 맵핑 7").type(RuleType.PRESCRIPTION).fromName("세레니아_정_24mg_4T").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 8").description("약품 맵핑 8").type(RuleType.PRESCRIPTION).fromName("베나실(benacil)_정_10mg_100T").toName("베나실 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 9").description("약품 맵핑 9").type(RuleType.PRESCRIPTION).fromName("암로디핀_정__5mg_30T").toName("암로디핀 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 10").description("약품 맵핑 10").type(RuleType.PRESCRIPTION).fromName("텔미로탄_정_40mg_30T").toName("텔미로탄 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 11").description("약품 맵핑 11").type(RuleType.PRESCRIPTION).fromName("텔미원_정_40mg_30T").toName("텔미원 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 12").description("약품 맵핑 12").type(RuleType.PRESCRIPTION).fromName("세레니아_정_16mg_4T").toName("세레니아 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 13").description("약품 맵핑 13").type(RuleType.PRESCRIPTION).fromName("텔미로탄").toName("텔미로탄 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 14").description("약품 맵핑 14").type(RuleType.PRESCRIPTION).fromName("S_Caninsulin 40IE/ml(캐닌슐린) 10ml_주사_주사비").toName("캐닌슐린 주사").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 15").description("약품 맵핑 15").type(RuleType.PRESCRIPTION).fromName("S_아스피린_정_100T/box").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
//        rules.add(StandardizedRule.builder().name("약품 맵핑 16").description("약품 맵핑 16").type(RuleType.PRESCRIPTION).fromName("아스피린_정_100mg_100T").toName("아스피린 정").hospital(hospital).created(LocalDateTime.now()).build());
//
//        ruleRepository.saveAll(rules);
//    }

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
}
