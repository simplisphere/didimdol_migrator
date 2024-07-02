package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.HospitalDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalDiagnosisRepository extends JpaRepository<HospitalDiagnosis, Long> {
}
