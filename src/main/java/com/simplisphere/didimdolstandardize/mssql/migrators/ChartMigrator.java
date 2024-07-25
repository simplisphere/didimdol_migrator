package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.rtfparserkit.converter.text.StringTextConverter;
import com.rtfparserkit.parser.RtfStreamSource;
import com.simplisphere.didimdolstandardize.mssql.entities.MsChart;
import com.simplisphere.didimdolstandardize.mssql.services.MsChartService;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChartMigrator {
    private final MsChartService chartService;
    private final PatientRepository patientRepository;


    public Page<Chart> convertChart(Hospital hospital, Pageable pageRequest) {
        Page<MsChart> legacyChartPage = chartService.findAll(pageRequest);

        Set<String> legacyPetIds = legacyChartPage.stream()
                .map(c -> c.getPet().getId().toString())
                .collect(Collectors.toSet());

        Map<String, Patient> patients = patientRepository.findByOriginalIdIn(legacyPetIds).stream()
                .collect(Collectors.toMap(Patient::getOriginalId, patient -> patient));

        List<Chart> newCharts = legacyChartPage
                .stream()
                .parallel()
                .map(c -> {
                    log.trace("original chart: {}", c.toString());
                    Patient patient = patients.get(c.getPet().getId().toString());
                    log.trace("found patient: {}", patient.toString());
                    Boolean isRtf = isRtf(c.getContent1());
                    String text = isRtf ? rtfParser(c.getContent1()) : c.getContent1();
                    String text2 = c.getContent2();
                    String doctorName = c.getDoctor() == null ? "" : c.getDoctor().getName().trim();
                    return Chart.builder()
                            .hospital(hospital).chartDate(c.getCreatedAt()).subject(text).cc(text2).objective("")
                            .originalId(c.getId().toString() + StringUtils.leftPad(c.getListOrder().toString(), 2, "0"))
                            .doctor(doctorName).created(c.getCreatedAt()).patient(patient)
                            .originalPetId(c.getPet().getId().toString()).build();
                }).toList();

        return new PageImpl<>(newCharts, legacyChartPage.getPageable(), legacyChartPage.getTotalElements());
    }

    private String rtfParser(String rtf) {
        StringTextConverter converter = new StringTextConverter();
        try (InputStream input = new ByteArrayInputStream(rtf.getBytes())) {
            converter.convert(new RtfStreamSource(input));
        } catch (IOException e) {
            log.error("Failed to parse RTF: {}", e.getMessage());
        }
        return converter.getText();
    }

    private Boolean isRtf(String text) {
        return text.startsWith("{\\rtf1");
    }
}
