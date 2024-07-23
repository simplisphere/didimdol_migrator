package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.VitalDto;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsVitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MsVitalService {
    private final MsVitalRepository msVitalRepository;

    public Page<VitalDto> findAll(Pageable pageRequest) {
        Page<Object[]> objects = msVitalRepository.findVitals(pageRequest);
        List<VitalDto> vitalDtos = objects.stream().map(result -> new VitalDto(
                ((Number) result[0]).intValue(), // vitalId
                ((Number) result[1]).intValue(), // petId
                toLocalDateTime((java.sql.Timestamp) result[2]),               // createdAt
                (String) result[3],               // bodyWeight
                (String) result[4],               // bodyTemperature
                (String) result[5],               // bloodPressure
                (String) result[6]                // doctor
        )).collect(Collectors.toList());

        return new PageImpl<>(vitalDtos, objects.getPageable(), objects.getTotalElements());
    }

    private LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
