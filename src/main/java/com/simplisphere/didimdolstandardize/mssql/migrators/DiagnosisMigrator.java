package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.entities.MsDiagnosis;
import com.simplisphere.didimdolstandardize.mssql.services.MsDiagnosisService;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.Diagnosis;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.HospitalDiagnosis;
import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import com.simplisphere.didimdolstandardize.postgresql.repositories.DiagnosisRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiagnosisMigrator {

    private final MsDiagnosisService msDiagnosisService;
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;

    // convert from MsDiagnosis to Diagnosis
    public Page<HospitalDiagnosis> convertDiagnosis(Hospital hospital, Pageable pageRequest) {
        log.info("Converting diagnoses...");
        Page<MsDiagnosis> legacyDiagnosisPage = msDiagnosisService.retrieveActiveDiagnoses(pageRequest);
        Set<String> legacyDiagnosisNames = legacyDiagnosisPage.stream()
                .map(msDiagnosis -> msDiagnosis.getDesc().trim())
                .collect(Collectors.toSet());

        List<StandardizedRule> ruleList = ruleRepository.findByTypeAndFromNameInAndHospital(RuleType.DIAGNOSIS, legacyDiagnosisNames, hospital);
        Map<String, StandardizedRule> rules = ruleList.stream().collect(Collectors.toMap(StandardizedRule::getFromName, rule -> rule));
        Set<String> ruleToNames = rules.values().stream().map(StandardizedRule::getToName).collect(Collectors.toSet());

        Map<String, Diagnosis> diagnoses = diagnosisRepository.findByNameIn(ruleToNames).stream()
                .collect(Collectors.toMap(Diagnosis::getName, diagnosis -> diagnosis));
        List<HospitalDiagnosis> newHospitalDiagnoses = legacyDiagnosisPage.stream().parallel().map(legacyDiagnosis -> {
                    StandardizedRule rule = rules.get(legacyDiagnosis.getDesc().trim());
                    // rule이 존재한다면 미리 조회한 diagnosis 맵에서 값을 가져와 대입
                    Diagnosis diagnosis = (rule != null) ? diagnoses.get(rule.getToName()) : null;
                    String descriptionEn = (legacyDiagnosis.getDescEn() != null) ? legacyDiagnosis.getDescEn().trim() : "";

                    return HospitalDiagnosis.builder()
                            .code(legacyDiagnosis.getCode().trim())
                            .name(legacyDiagnosis.getDesc().trim())
                            .description(descriptionEn)
                            .diagnosis(diagnosis)
                            .originalId(legacyDiagnosis.getId().toString())
                            .hospital(hospital)
                            .build();
                })
                .toList();
        return new PageImpl<>(newHospitalDiagnoses, legacyDiagnosisPage.getPageable(), legacyDiagnosisPage.getTotalElements());
    }
}
