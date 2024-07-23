package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.entities.MsAssessment;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MsAssessmentService {
    private final MsAssessmentRepository msAssessmentRepository;

    // retrieve assessments
    public List<MsAssessment> retrieveAssessments() {
        log.info("Retrieving assessments...");
        return msAssessmentRepository.findAll();
    }

    public Page<MsAssessment> findAssessments(Pageable pageRequest) {
        return msAssessmentRepository.findAll(pageRequest);
    }
}
