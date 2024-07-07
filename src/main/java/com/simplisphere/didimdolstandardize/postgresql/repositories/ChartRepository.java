package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartRepository extends JpaRepository<Chart, Long> {
    @EntityGraph(value = "Chart.withPatient", type = EntityGraph.EntityGraphType.LOAD)
    Chart findByOriginalId(String originalId);
}
