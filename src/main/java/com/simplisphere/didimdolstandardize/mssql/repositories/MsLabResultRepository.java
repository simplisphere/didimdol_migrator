package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsLabResult;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabResultId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MsLabResultRepository extends JpaRepository<MsLabResult, MsLabResultId> {
    @Query(value = "SELECT xlb.xlbplid AS laboratory_type_id, xlb.xlblbid AS laboratory_item_id, " +
            "hlb.hlbvsid, hlb.hlbplidx, hlb.hlbidx, hlb.hlbresult AS laboratory_result_result, " +
            "hlb.hlbindt AS laboratory_result_created, hlb.hlbptid AS pet_id " +
            "FROM hlb " +
            "JOIN hpl ON hlbvsid = hpl.hplvsid AND hlb.hlbplidx = hpl.hplidx " +
            "JOIN xlb ON hlb.hlblbid = xlb.xlblbid AND hpl.hplplid = xlb.xlbplid " +
            "JOIN pl p ON xlb.xlbplid = p.plid AND p.pllab = 1",
            countQuery = "SELECT COUNT(*) FROM hlb " +
                    "JOIN hpl ON hlbvsid = hpl.hplvsid AND hlb.hlbplidx = hpl.hplidx " +
                    "JOIN xlb ON hlb.hlblbid = xlb.xlblbid AND hpl.hplplid = xlb.xlbplid " +
                    "JOIN pl p ON xlb.xlbplid = p.plid AND p.pllab = 1",
            nativeQuery = true)
    Page<Object[]> findLabResults(Pageable pageRequest);
}
