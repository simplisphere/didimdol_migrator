package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulAssessment;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulAssessmentRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulAssessmentService {
    private final SosulAssessmentRepository sosulAssessmentRepository;

    // postgresql
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final ChartRepository chartRepository;
    private final PatientRepository patientRepository;

    // convert Assessment from original assessment
    public Page<Assessment> convertAssessmentFromOriginalAssessment(Hospital hospital, PageRequest pageRequest) {
        // original Assessment list 조회
        Page<SosulAssessment> sosulAssessments = sosulAssessmentRepository.findAll(pageRequest);

        Set<String> chartIdSet = sosulAssessments.stream()
                .map(SosulAssessment::getChart)
                .filter(Objects::nonNull)
                .map(chart -> chart.getId().toString())
                .collect(Collectors.toSet());

        Map<String, Chart> charts = chartRepository.findByOriginalIdIn(chartIdSet).stream()
                .collect(Collectors.toMap(Chart::getOriginalId, chart -> chart));

        Set<String> patientIdSet = sosulAssessments.stream()
                .map(sosulAssessment -> sosulAssessment.getPet().getId().toString())
                .collect(Collectors.toSet());

        Map<String, Patient> patients = patientRepository.findByOriginalIdIn(patientIdSet).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        Set<String> assessmentNameSet = sosulAssessments.stream()
                .map(SosulAssessment::getName)
                .collect(Collectors.toSet());

        // StandardizedRule을 미리 조회하여 맵으로 저장
        List<StandardizedRule> ruleList = ruleRepository.findByTypeAndFromNameInAndHospital(RuleType.DIAGNOSIS, assessmentNameSet, hospital);
        Map<String, StandardizedRule> rules = ruleList.stream()
                .collect(Collectors.toMap(StandardizedRule::getFromName, rule -> rule));

        // StandardizedRule에서 참조하는 모든 Diagnosis를 미리 조회하여 맵으로 저장
        Set<String> ruleToNames = rules.values().stream()
                .map(StandardizedRule::getToName)
                .collect(Collectors.toSet());

        Map<String, Diagnosis> diagnoses = diagnosisRepository.findByNameIn(ruleToNames).stream()
                .collect(Collectors.toMap(Diagnosis::getName, diagnosis -> diagnosis));

        // original Assessment -> Assessment 변환
        List<Assessment> newAssessments = sosulAssessments.stream().parallel().map(sosulAssessment -> {
                    Chart chart = null;
                    if (sosulAssessment.getChart() != null) {
                        chart = charts.get(sosulAssessment.getChart().getId().toString());
                    }
                    Patient patient = patients.get(sosulAssessment.getPet().getId().toString());
                    log.trace("original assessment name: {}", sosulAssessment.getName());
                    StandardizedRule rule = rules.get(sosulAssessment.getName());
                    // rule이 존재한다면 미리 조회한 diagnosis 맵에서 값을 가져와 대입
                    Diagnosis diagnosis = (rule != null) ? diagnoses.get(rule.getToName()) : null;
                    return Assessment.builder()
                            .name(sosulAssessment.getName())
                            .status(AssessmentStatus.Final_Diagnosis)
                            .originalId(sosulAssessment.getId().toString())
                            .originalPetId(sosulAssessment.getPet().getId().toString())
                            .doctor(sosulAssessment.getSign())
                            .chart(chart)
                            .diagnosis(diagnosis)
                            .patient(patient)
                            .hospital(hospital)
                            .build();
                })
                .toList();

        return new PageImpl<>(newAssessments, sosulAssessments.getPageable(), sosulAssessments.getTotalElements());
    }
}
