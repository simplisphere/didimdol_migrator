package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class LaboratoryItemRepositoryCustomImpl implements LaboratoryItemRepositoryCustom {

    @PersistenceContext(unitName = "postgresqlEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true, transactionManager = "postgresqlTransactionManager")
    public List<LaboratoryItem> findByLegacyLabItemIdAndLegacyLabTypeIdIn(Set<Pair<String, String>> legacyLabItemAndTypeIds) {
        String sql = "SELECT li.id AS li_id, li.name AS li_name, li.code AS li_code, li.abbreviation AS li_abbreviation, " +
                "li.description AS li_description, li.unit AS li_unit, li.original_id AS li_original_id, " +
                "li.order_idx AS li_order_idx, li.hospital_id AS li_hospital_id, li.created AS li_created, " +
                "li.updated AS li_updated, li.laboratory_type_id AS li_laboratory_type_id, " +
                "lt.id AS lt_id, lt.name AS lt_name, lt.abbreviation AS lt_abbreviation, " +
                "lt.description AS lt_description, lt.hospital_id AS lt_hospital_id, " +
                "lt.original_id AS lt_original_id, lt.created AS lt_created, lt.updated AS lt_updated " +
                "FROM laboratory_item li " +
                "JOIN laboratory_type lt ON li.laboratory_type_id = lt.id " +
                "WHERE (li.original_id, lt.original_id) IN (" +
                legacyLabItemAndTypeIds.stream()
                        .map(pair -> "(?, ?)")
                        .collect(Collectors.joining(", ")) +
                ")";

        Query query = entityManager.createNativeQuery(sql, "LaboratoryItemWithTypeMapping");

        int index = 1;
        for (Pair<String, String> pair : legacyLabItemAndTypeIds) {
            query.setParameter(index++, pair.getLeft());
            query.setParameter(index++, pair.getRight());
        }

        return query.getResultList();
    }
}
