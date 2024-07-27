package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsLabItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MsLabItemRepository extends JpaRepository<MsLabItem, Integer> {
    @Query(value = "SELECT lb.lbid AS id, xlb.xlblstord AS listOrder, lb.lbdesc AS name, lb.lbunit AS unit, p.plid AS productId, p.plcode AS productCode " +
            "FROM lb " +
            "JOIN xlb ON lb.lbid = xlb.xlblbid " +
            "JOIN pl p ON xlb.xlbplid = p.plid " +
            "WHERE p.pllab = 1", nativeQuery = true)
    Page<Object[]> findLabItems(Pageable pageRequest);
}
