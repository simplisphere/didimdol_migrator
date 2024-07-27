package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface LaboratoryItemRepository extends JpaRepository<LaboratoryItem, Long>, LaboratoryItemRepositoryCustom {
    List<LaboratoryItem> findByOriginalIdIn(Set<String> originalIds);

//    @Query("SELECT li FROM LaboratoryItem li " +
//            "JOIN FETCH li.type lt " +
//            "WHERE (li.originalId, lt.originalId) IN :pairs")
//    List<LaboratoryItem> findByLegacyLabItemIdAndLegacyLabTypeIdIn(@Param("pairs") List<String[]> pairs);

//    @Query(value = "SELECT li.* FROM laboratory_item li " +
//            "JOIN laboratory_type lt ON li.laboratory_type_id = lt.id " +
//            "WHERE (li.original_id, lt.original_id) IN :pairs", nativeQuery = true)
//    List<LaboratoryItem> findByLegacyLabItemIdAndLegacyLabTypeIdIn(@Param("pairs") List<String[]> pairs);
}
