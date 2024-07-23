package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsChart;
import com.simplisphere.didimdolstandardize.mssql.entities.MsChartId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MsChartRepository extends JpaRepository<MsChart, MsChartId> {
    @EntityGraph(value = "MsChart.withFetchJoin", type = EntityGraph.EntityGraphType.LOAD)
    Page<MsChart> findAll(Pageable pageRequest);
}
