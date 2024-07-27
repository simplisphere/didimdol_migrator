package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public interface LaboratoryItemRepositoryCustom {
    List<LaboratoryItem> findByLegacyLabItemIdAndLegacyLabTypeIdIn(Set<Pair<String, String>> legacyLabItemAndTypeIds);
}
