package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizeDiagnosisMarker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardMarkerRepository extends JpaRepository<StandardizeDiagnosisMarker, Long> {
}
