package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsDiagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MsDiagnosisRepository extends JpaRepository<MsDiagnosis, String> {
    List<MsDiagnosis> findAllByActiveTrue();

    Page<MsDiagnosis> findAllByActiveTrue(Pageable pageable);
}
