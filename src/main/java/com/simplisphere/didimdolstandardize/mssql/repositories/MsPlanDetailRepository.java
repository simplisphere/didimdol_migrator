package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsPlanDetail;
import com.simplisphere.didimdolstandardize.mssql.entities.MsPlanDetailId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsPlanDetailRepository extends JpaRepository<MsPlanDetail, MsPlanDetailId> {
    @EntityGraph(value = "MsPlanDetail.withFetchJoin", type = EntityGraph.EntityGraphType.LOAD)
    @Query(value = "SELECT h FROM MsPlanDetail h " +
            "LEFT JOIN h.doctor d " +
            "WHERE h.plan.id IN (" +
            "    SELECT p.id " +
            "    FROM MsPlan p " +
            "    WHERE p.type IN :types" +
            ")")
    Page<MsPlanDetail> findByPlanTypeIn(Pageable pageRequest, List<Integer> types);
}
