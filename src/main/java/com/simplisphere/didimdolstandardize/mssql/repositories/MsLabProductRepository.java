package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsLabProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MsLabProductRepository extends JpaRepository<MsLabProduct, Integer> {
    Page<MsLabProduct> findAllByIsLaboratory(Pageable pageRequest, Boolean b);
}
