package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Vital;
import com.simplisphere.didimdolstandardize.postgresql.repositories.VitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VitalService {
    private final VitalRepository vitalRepository;

    // saveAll Charts
    public List<Vital> saveAll(List<Vital> charts) {
        return vitalRepository.saveAll(charts);
    }
}
