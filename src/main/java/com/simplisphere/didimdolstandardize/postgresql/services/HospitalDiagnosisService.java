package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.HospitalDiagnosis;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalDiagnosisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HospitalDiagnosisService {
    private final HospitalDiagnosisRepository hospitalDiagnosisRepository;

    // hospital save all
    public List<HospitalDiagnosis> saveAll(List<HospitalDiagnosis> hospitalDiagnoses) {
        return hospitalDiagnosisRepository.saveAll(hospitalDiagnoses);
    }
}
