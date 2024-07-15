package com.simplisphere.didimdolstandardize.postgresql.repositories;

import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RuleRepository extends JpaRepository<StandardizedRule, Long> {
    Optional<StandardizedRule> findByFromNameAndHospital(String name, Hospital hospital);

    Optional<StandardizedRule> findByTypeAndFromNameAndHospital(RuleType type, String name, Hospital hospital);

    List<StandardizedRule> findByTypeAndFromNameInAndHospital(RuleType type, Set<String> fromNames, Hospital hospital);
}
