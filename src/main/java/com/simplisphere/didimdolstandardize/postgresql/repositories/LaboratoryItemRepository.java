package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaboratoryItemRepository extends JpaRepository<LaboratoryItem, Long> {
    Optional<LaboratoryItem> findByName(String name);

    Optional<LaboratoryItem> findByOriginalId(String originalId);
}
