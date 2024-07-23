package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChartService {
    private final ChartRepository chartRepository;

    // saveAll Charts
    public List<Chart> saveAll(List<Chart> charts) {
        return chartRepository.saveAll(charts);
    }
}
