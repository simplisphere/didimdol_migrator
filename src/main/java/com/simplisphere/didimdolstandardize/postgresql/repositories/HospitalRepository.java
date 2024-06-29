package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    boolean existsByName(String name);

    Optional<Hospital> findByName(String name);
}
