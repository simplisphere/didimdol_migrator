package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizeDiagnosisMarker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardMarkerRepository extends JpaRepository<StandardizeDiagnosisMarker, Long> {
    List<StandardizeDiagnosisMarker> findByNameIn(List<String> names);
}
