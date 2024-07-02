package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulDiagnosis;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulDxRepository;
import com.simplisphere.didimdolstandardize.postgresql.entities.Diagnosis;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.HospitalDiagnosis;
import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import com.simplisphere.didimdolstandardize.postgresql.repositories.DiagnosisRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalDiagnosisRepository;
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
public class SosulDiagnosisService {

    // firebird
    private final SosulDxRepository sosulDxRepository;

    // postgresql
    private final RuleRepository ruleRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final HospitalDiagnosisRepository hospitalDiagnosisRepository;

    public Page<HospitalDiagnosis> saveHospitalDiagnosisAndSaveStandardDiagnosisFromSosulDiagnosis(Hospital hospital, PageRequest pageRequest) {
        // Sosul Diagnosis list 조회
        Page<SosulDiagnosis> sosulDiagnoses = sosulDxRepository.findAll(pageRequest);

        // Sosul Diagnosis -> Hospital Diagnosis 변환
        List<HospitalDiagnosis> newHospitalDiagnoses = sosulDiagnoses.stream().parallel().map(sosulDiagnosis -> {
                    Optional<StandardizedRule> rule = ruleRepository.findByFromName(sosulDiagnosis.getName());
                    // rule이 존재한다면 diagnosis에서 rule.toName과 같은 이름을 가진 객체를 조회하여 대입
                    Optional<Diagnosis> diagnosis = rule.flatMap(standardizedRule -> diagnosisRepository.findByName(standardizedRule.getToName()));
                    return HospitalDiagnosis.builder()
                            .code(sosulDiagnosis.getCode())
                            .name(sosulDiagnosis.getName())
                            .description(sosulDiagnosis.getStdName())
                            .diagnosis(diagnosis.orElse(null))
                            .originalId(sosulDiagnosis.getId().toString())
                            .hospital(hospital)
                            .build();
                })
                .toList();

        return new PageImpl<>(newHospitalDiagnoses, sosulDiagnoses.getPageable(), sosulDiagnoses.getTotalElements());
    }
}
