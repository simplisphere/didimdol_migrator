package com.simplisphere.didimdolstandardize.firebird.repositories;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulLabResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosulLabResultRepository extends JpaRepository<SosulLabResult, Long> {
    @EntityGraph(value = "SosulLabResult.withLabDateAndLabItem", type = EntityGraph.EntityGraphType.FETCH)
    Page<SosulLabResult> findAll(Pageable pageable);
}
