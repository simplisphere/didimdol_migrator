package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.entities.MsAssessment;
import com.simplisphere.didimdolstandardize.mssql.services.MsAssessmentService;
import com.simplisphere.didimdolstandardize.postgresql.AssessmentStatus;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.*;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.DiagnosisRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class AssessmentMigrator {
    private final MsAssessmentService msAssessmentService;
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final ChartRepository chartRepository;

    public Page<Assessment> convertAssessment(Hospital hospital, Pageable pageRequest) {
        // original Assessment list 조회
        Page<MsAssessment> legacyAssessmentPage = msAssessmentService.findAssessments(pageRequest);

        Set<String> legacyChartIdSet = legacyAssessmentPage.stream()
                .map(msAssessment -> msAssessment.getChartId().toString() + "01")
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Chart> charts = chartRepository.findByOriginalIdIn(legacyChartIdSet).stream()
                .collect(Collectors.toMap(Chart::getOriginalId, chart -> chart));

        Set<String> patientIdSet = legacyAssessmentPage.stream()
                .map(legacyAssessment -> legacyAssessment.getPet().getId().toString())
                .collect(Collectors.toSet());

        Map<String, Patient> patients = patientRepository.findByOriginalIdIn(patientIdSet).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        Set<String> legacyAssessmentNames = legacyAssessmentPage.stream()
                .map(MsAssessment::getName)
                .collect(Collectors.toSet());

        List<StandardizedRule> ruleList = ruleRepository.findByTypeAndFromNameInAndHospital(RuleType.DIAGNOSIS, legacyAssessmentNames, hospital);
        Map<String, StandardizedRule> rules = ruleList.stream().collect(Collectors.toMap(StandardizedRule::getFromName, rule -> rule));
        Set<String> ruleToNames = rules.values().stream().map(StandardizedRule::getToName).collect(Collectors.toSet());

        Map<String, Diagnosis> diagnoses = diagnosisRepository.findByNameIn(ruleToNames).stream()
                .collect(Collectors.toMap(Diagnosis::getName, diagnosis -> diagnosis));
        // original Assessment -> Assessment 변환
        List<Assessment> newAssessments = legacyAssessmentPage.stream().parallel().map(legacyAssessment -> {
                    Chart chart = null;
                    if (legacyAssessment.getChartId() != null) {
                        chart = charts.get(legacyAssessment.getChartId().toString() + "01");
                    }
                    Patient patient = patients.get(legacyAssessment.getPet().getId().toString());
                    log.trace("original assessment name: {}", legacyAssessment.getName());
                    StandardizedRule rule = rules.get(legacyAssessment.getName());
                    // rule이 존재한다면 미리 조회한 diagnosis 맵에서 값을 가져와 대입
                    Diagnosis diagnosis = (rule != null) ? diagnoses.get(rule.getToName()) : null;
                    return Assessment.builder()
                            .name(legacyAssessment.getName())
                            .status(AssessmentStatus.Final_Diagnosis)
                            .originalId(legacyAssessment.getId().toString())
                            .originalPetId(legacyAssessment.getPet().getId().toString())
                            .doctor(legacyAssessment.getAssessmentDoctor().getName())
                            .chart(chart)
                            .diagnosis(diagnosis)
                            .patient(patient)
                            .hospital(hospital)
                            .build();
                })
                .toList();

        return new PageImpl<>(newAssessments, legacyAssessmentPage.getPageable(), legacyAssessmentPage.getTotalElements());
    }
}
