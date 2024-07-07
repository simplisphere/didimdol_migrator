package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.firebird.SosulChart;
import com.simplisphere.didimdolstandardize.firebird.SosulChartRepository;
import com.simplisphere.didimdolstandardize.firebird.SosulClient;
import com.simplisphere.didimdolstandardize.firebird.SosulPet;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulPetRepository;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StandardizeSosulService {
    private final SosulPetRepository sosulPetRepository;
    private final SosulChartRepository sosulChartRepository;

    private final PatientRepository patientRepository;
    private final ChartRepository chartRepository;

    @Transactional
    public Page<Patient> standardizedPatient(Hospital hospital, PageRequest pageRequest) {
        Page<Pair<SosulPet, SosulClient>> petsAndClients = sosulPetRepository.findAllPetsAndClients(pageRequest);
        List<Patient> patients = saveAllPatientsFromOrigin(
                hospital,
                petsAndClients.get()
//                        .filter(p -> p.a.getSosulOriginSpecies().toSpecies() != Species.ETC)
                        .toList()
        );

        return new PageImpl<>(patients, petsAndClients.getPageable(), petsAndClients.getTotalElements());
    }

    @Transactional
    public Page<Chart> standardizedChart(Hospital hospital, PageRequest pageRequest) {
        Page<SosulChart> originCharts = sosulChartRepository.findAll(pageRequest);
        List<Chart> newCharts = originCharts
//                .filter(c -> c.getSosulPet().getSosulOriginSpecies().toSpecies() != Species.ETC)
                .map(c -> {
                    log.trace("original chart: {}", c.toString());
                    Patient patient = patientRepository.findByOriginalId(c.getSosulPet().getId().toString());
                    log.trace("found patient: {}", patient.toString());
                    LocalDateTime dateTime = c.getDate().atTime(c.getTime());
                    return Chart.builder().hospital(hospital).chartDate(dateTime).subject(c.getMemo()).cc(c.getCc()).objective("")
                            .originalId(c.getId().toString()).doctor(c.getSign()).created(dateTime).patient(patient)
                            .originalPetId(c.getSosulPet().getId().toString()).build();
                }).toList();

        return new PageImpl<>(chartRepository.saveAll(newCharts), originCharts.getPageable(), originCharts.getTotalElements());
    }


    private List<Patient> saveAllPatientsFromOrigin(Hospital hospital, List<Pair<SosulPet, SosulClient>> petsAndClients) {
        List<Patient> newPatients = petsAndClients.stream().map(p -> {
            SosulPet sosulPet = p.a;
            SosulClient sosulClient = p.b;

            log.trace("original pet: {}", sosulPet.toString());
            Patient patient = Patient.builder().name(sosulPet.getName()).clientName(sosulClient.getName()).breed(sosulPet.getBreed())
                    .birth(sosulPet.getBirth()).address("").phone("").sex(sosulPet.getSex()).originalId(sosulPet.getId().toString())
                    .species(sosulPet.getSosulOriginSpecies().toSpecies()).hospital(hospital).build();
            log.trace("convert patient: {}", patient.toString());
            return patient;
        }).toList();

        return patientRepository.saveAll(newPatients);
    }
}
