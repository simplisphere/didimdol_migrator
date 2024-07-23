package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MsAssessmentRepository extends JpaRepository<MsAssessment, Integer> {
    @EntityGraph(value = "MsAssessment.withFetchJoin", type = EntityGraph.EntityGraphType.LOAD)
    Page<MsAssessment> findAll(Pageable pageRequest);
}
