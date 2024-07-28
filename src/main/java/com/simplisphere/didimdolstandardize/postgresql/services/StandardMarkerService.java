package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizeDiagnosisMarker;
import com.simplisphere.didimdolstandardize.postgresql.repositories.StandardMarkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StandardMarkerService {
    private final StandardMarkerRepository standardMarkerRepository;

    public List<StandardizeDiagnosisMarker> saveAll(List<StandardizeDiagnosisMarker> markers) {
        List<StandardizeDiagnosisMarker> existMarkers = standardMarkerRepository.findByNameIn(markers.stream().map(StandardizeDiagnosisMarker::getName).toList());
        log.debug("markers size: {}", markers.size());
        ArrayList<StandardizeDiagnosisMarker> deduplicatedMarkers = new ArrayList<>(markers.stream().distinct().toList());
        deduplicatedMarkers.removeAll(existMarkers);
        log.debug("markers size after remove: {}", deduplicatedMarkers.size());
        return standardMarkerRepository.saveAll(deduplicatedMarkers);
    }
}
