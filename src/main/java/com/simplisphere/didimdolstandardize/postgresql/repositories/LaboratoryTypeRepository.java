package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaboratoryTypeRepository extends JpaRepository<LaboratoryType, Long> {
    Optional<LaboratoryType> findByOriginalId(String originalId);
}
