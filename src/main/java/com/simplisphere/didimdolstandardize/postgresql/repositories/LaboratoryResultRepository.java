package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratoryResultRepository extends JpaRepository<LaboratoryResult, Long> {
}
