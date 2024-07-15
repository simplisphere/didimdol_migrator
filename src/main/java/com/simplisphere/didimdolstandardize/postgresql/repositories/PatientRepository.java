package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByOriginalId(String originalId);

    List<Patient> findByOriginalIdIn(Set<String> originalIds);
}
