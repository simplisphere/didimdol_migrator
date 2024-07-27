package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsLabReference;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabReferenceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MsLabReferenceRepository extends JpaRepository<MsLabReference, MsLabReferenceId> {
    // find in species id
    @Query("SELECT m FROM MsLabReference m WHERE m.speciesId IN :speciesIds")
    Page<MsLabReference> findBySpeciesIdIn(Pageable pageRequest, List<Integer> speciesIds);
}
