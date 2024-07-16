package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface LaboratoryItemRepository extends JpaRepository<LaboratoryItem, Long> {
    List<LaboratoryItem> findByOriginalIdIn(Set<String> originalIds);
}
