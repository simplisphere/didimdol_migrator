package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.entities.MsPlanDetail;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsPlanDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MsPlanService {
    private final MsPlanDetailRepository msPlanDetailRepository;

    public Page<MsPlanDetail> retrievePlanDetails(Pageable pageRequest) {
        return msPlanDetailRepository.findByPlanTypeIn(pageRequest, List.of(75, 83, 85));
    }
}
