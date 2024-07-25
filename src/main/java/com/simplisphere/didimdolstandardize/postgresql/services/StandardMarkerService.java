package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizeDiagnosisMarker;
import com.simplisphere.didimdolstandardize.postgresql.repositories.StandardMarkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StandardMarkerService {
    private final StandardMarkerRepository standardMarkerRepository;


    public List<StandardizeDiagnosisMarker> saveAll(List<StandardizeDiagnosisMarker> markers) {
        return standardMarkerRepository.saveAll(markers);
    }
}
