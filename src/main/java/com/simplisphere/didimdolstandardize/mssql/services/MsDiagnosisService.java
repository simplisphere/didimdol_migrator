package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.entities.MsDiagnosis;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsDiagnosisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MsDiagnosisService {
    private final MsDiagnosisRepository msDiagnosisRepository;

    // retrieve active diagnoses
    public List<MsDiagnosis> retrieveActiveDiagnoses() {
        log.info("Retrieving active diagnoses...");
        return msDiagnosisRepository.findAllByActiveTrue();
    }

    // retreive diagnoses with pageable
    public Page<MsDiagnosis> retrieveActiveDiagnoses(Pageable pageRequest) {
        log.info("Retrieving active diagnoses with pageable...");
        return msDiagnosisRepository.findAllByActiveTrue(pageRequest);
    }

}
