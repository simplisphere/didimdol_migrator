package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Diagnosis;
import com.simplisphere.didimdolstandardize.postgresql.repositories.DiagnosisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;

    // if exist then retrieve, else save
    public Diagnosis findOrCreate(Diagnosis diagnosis) {
        return diagnosisRepository.findByName(diagnosis.getName())
                .orElseGet(() -> diagnosisRepository.save(diagnosis));
    }
}
