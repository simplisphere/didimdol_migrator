package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulChart;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SosulChartService {
    private final SosulChartRepository sosulChartRepository;

    // postgresql
    private final PatientRepository patientRepository;

    public Page<Chart> standardizeChart(Hospital hospital, PageRequest pageRequest) {
        Page<SosulChart> originCharts = sosulChartRepository.findAll(pageRequest);

        Set<String> patientIdSet = originCharts.stream()
                .map(c -> c.getSosulPet().getId().toString())
                .collect(Collectors.toSet());

        Map<String, Patient> patients = patientRepository.findByOriginalIdIn(patientIdSet).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        List<Chart> newCharts = originCharts
                .stream()
                .parallel()
//                .filter(c -> c.getSosulPet().getSosulOriginSpecies().toSpecies() != Species.ETC)
                .map(c -> {
                    log.trace("original chart: {}", c.toString());
                    Patient patient = patients.get(c.getSosulPet().getId().toString());
                    log.trace("found patient: {}", patient.toString());
                    LocalDateTime dateTime = c.getDate().atTime(c.getTime());
                    return Chart.builder().hospital(hospital).chartDate(dateTime).subject(c.getMemo()).cc(c.getCc()).objective("")
                            .originalId(c.getId().toString()).doctor(c.getSign()).created(dateTime).patient(patient)
                            .originalPetId(c.getSosulPet().getId().toString()).build();
                }).toList();

        return new PageImpl<>(newCharts, originCharts.getPageable(), originCharts.getTotalElements());
    }
}
