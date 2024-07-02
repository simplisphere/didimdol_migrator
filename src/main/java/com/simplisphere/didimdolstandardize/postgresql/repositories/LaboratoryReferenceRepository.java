package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratoryReferenceRepository extends JpaRepository<LaboratoryReference, Long> {
}
