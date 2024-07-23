package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RuleService {
    private final RuleRepository ruleRepository;

    // save all
    public List<StandardizedRule> saveAll(List<StandardizedRule> rules) {
        return ruleRepository.saveAll(rules);
    }
}
