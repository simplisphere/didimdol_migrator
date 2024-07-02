package com.simplisphere.didimdolstandardize.firebird.repositories;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosulDxRepository extends JpaRepository<SosulDiagnosis, Integer> {
}
