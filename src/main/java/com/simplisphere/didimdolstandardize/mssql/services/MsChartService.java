package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.entities.MsChart;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MsChartService {
    private final MsChartRepository msChartRepository;

    // retrieve charts
    public Page<MsChart> findAll(Pageable pageRequest) {
        return msChartRepository.findAll(pageRequest);
    }
}
