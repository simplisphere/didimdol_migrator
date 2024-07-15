package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulDiagnosis;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulDxRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulDiagnosisService {

    // firebird
    private final SosulDxRepository sosulDxRepository;

    // postgresql
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;

    public Page<HospitalDiagnosis> convertHospitalDiagnosisFromSosulDiagnosis(Hospital hospital, PageRequest pageRequest) {
        // Sosul Diagnosis list 조회
        Page<SosulDiagnosis> sosulDiagnoses = sosulDxRepository.findAll(pageRequest);

        Set<String> diagnosisNames = sosulDiagnoses.stream()
                .map(SosulDiagnosis::getName)
                .collect(Collectors.toSet());

        List<StandardizedRule> ruleList = ruleRepository.findByTypeAndFromNameInAndHospital(RuleType.DIAGNOSIS, diagnosisNames, hospital);
        Map<String, StandardizedRule> rules = ruleList.stream().collect(Collectors.toMap(StandardizedRule::getFromName, rule -> rule));
        Set<String> ruleToNames = rules.values().stream().map(StandardizedRule::getToName).collect(Collectors.toSet());

        Map<String, Diagnosis> diagnoses = diagnosisRepository.findByNameIn(ruleToNames).stream()
                .collect(Collectors.toMap(Diagnosis::getName, diagnosis -> diagnosis));

        // Sosul Diagnosis -> Hospital Diagnosis 변환
        List<HospitalDiagnosis> newHospitalDiagnoses = sosulDiagnoses.stream().parallel().map(sosulDiagnosis -> {
                    StandardizedRule rule = rules.get(sosulDiagnosis.getName());
                    // rule이 존재한다면 미리 조회한 diagnosis 맵에서 값을 가져와 대입
                    Diagnosis diagnosis = (rule != null) ? diagnoses.get(rule.getToName()) : null;
                    return HospitalDiagnosis.builder()
                            .code(sosulDiagnosis.getCode())
                            .name(sosulDiagnosis.getName())
                            .description(sosulDiagnosis.getStdName())
                            .diagnosis(diagnosis)
                            .originalId(sosulDiagnosis.getId().toString())
                            .hospital(hospital)
                            .build();
                })
                .toList();

        return new PageImpl<>(newHospitalDiagnoses, sosulDiagnoses.getPageable(), sosulDiagnoses.getTotalElements());
    }
}
