package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameIn(Set<String> names);
}
