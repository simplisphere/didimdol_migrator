package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulClient;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulPet;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulPetRepository;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SosulPetService {
    private final SosulPetRepository sosulPetRepository;

    @Transactional
    public Page<Patient> standardizePatient(Hospital hospital, PageRequest pageRequest) {
        Page<Pair<SosulPet, SosulClient>> petsAndClients = sosulPetRepository.findAllPetsAndClients(pageRequest);
        List<Patient> patients = petsAndClients.stream()
                .parallel()
//                .filter(p -> p.a.getSosulOriginSpecies().toSpecies() != Species.ETC)
                .map(p -> {
                    SosulPet sosulPet = p.a;
                    SosulClient sosulClient = p.b;

                    log.trace("original pet: {}", sosulPet.toString());
                    Patient patient = Patient.builder().name(sosulPet.getName()).clientName(sosulClient.getName()).breed(sosulPet.getBreed())
                            .birth(sosulPet.getBirth()).address("").phone("").sex(sosulPet.getSex()).originalId(sosulPet.getId().toString())
                            .species(sosulPet.getSosulOriginSpecies().toSpecies()).hospital(hospital).build();
                    log.trace("convert patient: {}", patient.toString());
                    return patient;
                }).toList();

        return new PageImpl<>(patients, petsAndClients.getPageable(), petsAndClients.getTotalElements());
    }
}
