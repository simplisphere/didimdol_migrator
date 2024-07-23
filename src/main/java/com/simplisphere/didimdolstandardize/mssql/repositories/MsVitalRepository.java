package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsVital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MsVitalRepository extends JpaRepository<MsVital, Integer> {
    @Query(
            value = "SELECT hcl.hclid AS vital_id, hcl.hclptid AS pet_id, hcl.hclrdt AS created_at, " +
                    "(SELECT hck.hckresult FROM hck WHERE hck.hckhclid = hcl.hclid AND hck.hckckid = 0) AS body_weight, " +
                    "(SELECT hck.hckresult FROM hck WHERE hck.hckhclid = hcl.hclid AND hck.hckckid = 1) AS body_temperature, " +
                    "(SELECT hck.hckresult FROM hck WHERE hck.hckhclid = hcl.hclid AND hck.hckckid = 7) AS blood_pressure, " +
                    "em.emname AS doctor " +
                    "FROM hcl " +
                    "LEFT JOIN em ON hcl.hclremid = em.emid " +
                    "ORDER BY hcl.hclrdt DESC, hcl.hclid",
            countQuery = "SELECT COUNT(*) FROM hcl",
            nativeQuery = true)
    Page<Object[]> findVitals(Pageable pageable);
}
