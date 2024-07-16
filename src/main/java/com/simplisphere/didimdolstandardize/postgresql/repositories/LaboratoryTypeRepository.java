package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface LaboratoryTypeRepository extends JpaRepository<LaboratoryType, Long> {
    List<LaboratoryType> findByOriginalIdIn(Set<String> originalIds);
}
