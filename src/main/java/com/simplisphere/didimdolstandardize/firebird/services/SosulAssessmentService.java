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
import java.util.Optional;

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

        // original Assessment -> Assessment 변환
        List<Assessment> newAssessments = sosulAssessments.stream().parallel().map(sosulAssessment -> {
                    Chart chart = null;
                    if (sosulAssessment.getChart() != null) {
                        chart = chartRepository.findByOriginalId(sosulAssessment.getChart().getId().toString());
                    }
                    Patient patient = patientRepository.findByOriginalId(sosulAssessment.getPet().getId().toString());
                    log.trace("original assessment name: {}", sosulAssessment.getName());
                    Optional<StandardizedRule> rule = ruleRepository.findByTypeAndFromNameAndHospital(RuleType.DIAGNOSIS, sosulAssessment.getName(), hospital);
                    // rule이 존재한다면 diagnosis에서 rule.toName과 같은 이름을 가진 객체를 조회하여 대입
                    Optional<Diagnosis> diagnosis = rule.flatMap(standardizedRule -> diagnosisRepository.findByName(standardizedRule.getToName()));
                    return Assessment.builder()
                            .name(sosulAssessment.getName())
                            .status(AssessmentStatus.Final_Diagnosis)
                            .originalId(sosulAssessment.getId().toString())
                            .originalPetId(sosulAssessment.getPet().getId().toString())
                            .doctor(sosulAssessment.getSign())
                            .chart(chart)
                            .diagnosis(diagnosis.orElse(null))
                            .patient(patient)
                            .hospital(hospital)
                            .build();
                })
                .toList();

        return new PageImpl<>(newAssessments, sosulAssessments.getPageable(), sosulAssessments.getTotalElements());
    }
}
