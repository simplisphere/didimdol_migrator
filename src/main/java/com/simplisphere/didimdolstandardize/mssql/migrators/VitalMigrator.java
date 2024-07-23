package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.VitalDto;
import com.simplisphere.didimdolstandardize.mssql.services.MsVitalService;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.Vital;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Component
public class VitalMigrator {
    private final MsVitalService msVitalService;
    private final PatientRepository patientRepository;

    public Page<Vital> convertVital(Hospital hospital, Pageable pageRequest) {
        Page<VitalDto> legacyVitals = msVitalService.findAll(pageRequest);

        // 필요한 Patient의 ID를 Set으로 수집
        Set<String> legacyPetIds = legacyVitals.stream()
                .map(vitalDto -> vitalDto.getPetId().toString())
                .collect(Collectors.toSet());

        // 모든 필요한 Patient를 미리 조회하여 맵으로 저장
        Map<String, Patient> patientMap = patientRepository.findByOriginalIdIn(legacyPetIds).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        // original Vital -> Vital 변환
        List<Vital> newVitals = legacyVitals.stream().parallel().map(vitalDto -> {
            Patient patient = patientMap.get(vitalDto.getPetId().toString());
            Float bodyWeight = convertToFloat(vitalDto.getBodyWeight());
            Float bloodPressure = convertToFloat(vitalDto.getBloodPressure());
            Float temperature = convertToFloat(vitalDto.getBodyTemperature());

            return Vital.builder()
                    .temperature(temperature)
                    .pulse(0F)
                    .respiratoryRate(0F)
                    .heartRate(0F)
                    .bodyWeight(bodyWeight)
                    .bloodPressure(bloodPressure)
                    .doctor(vitalDto.getDoctor())
                    .originalId(vitalDto.getVitalId().toString())
                    .originalPetId(vitalDto.getPetId().toString())
                    .created(vitalDto.getCreatedAt())
                    .patient(patient)
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(newVitals, legacyVitals.getPageable(), legacyVitals.getTotalElements());
    }

    private Float convertToFloat(String str) {
        return isNumeric(str) ? Float.parseFloat(str) : 0;
    }

    private Boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        return str.trim().matches("-?\\d+(\\.\\d+)?");
    }
}
