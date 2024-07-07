package com.simplisphere.didimdolstandardize.firebird.repositories;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulTrxData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SosulTrxDataRepository extends JpaRepository<SosulTrxData, Long> {

    @EntityGraph(value = "SosulTrxData.chartAndPet", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT t FROM SosulTrxData t WHERE t.route = :route OR t.name LIKE %:name% ORDER BY t.chart.id DESC, t.index ASC")
    Page<SosulTrxData> findByRouteOrName(@Param("route") String route, @Param("name") String name, Pageable pageable);
}
