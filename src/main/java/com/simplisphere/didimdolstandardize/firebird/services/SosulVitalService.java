package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulVital;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulVitalRepository;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.Vital;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulVitalService {
    private final SosulVitalRepository sosulVitalRepository;
    private final PatientRepository patientRepository;


    // convert sosul vital from vital
    public Page<Vital> convertVitalFromSosulVital(Hospital hospital, PageRequest pageRequest) {
        // original Vital list 조회
        Page<SosulVital> sosulVitals = sosulVitalRepository.findAll(pageRequest);

        // original Vital -> Vital
        List<Vital> newVitals = sosulVitals.stream().parallel().map(sosulVital -> {
            Patient patient = patientRepository.findByOriginalId(sosulVital.getPet().getId().toString());
            return Vital.builder()
                    .temperature(sosulVital.getBt())
                    .pulse(sosulVital.getBp())
                    .respiratoryRate(sosulVital.getRr())
                    .heartRate(sosulVital.getHr())
                    .bodyWeight(sosulVital.getBw())
                    .bloodPressure(sosulVital.getBp2())
                    .doctor(sosulVital.getSignName())
                    .originalId(sosulVital.getId().toString())
                    .originalPetId(sosulVital.getPet().getId().toString())
                    .created(sosulVital.getDate().atTime(sosulVital.getTime()))
                    .patient(patient)
                    .hospital(hospital)
                    .build();
        }).toList();

        return new PageImpl<>(newVitals, sosulVitals.getPageable(), sosulVitals.getTotalElements());
    }
}
