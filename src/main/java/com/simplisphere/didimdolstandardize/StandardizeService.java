package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.firebird.Client;
import com.simplisphere.didimdolstandardize.firebird.Pet;
import com.simplisphere.didimdolstandardize.firebird.PetRepository;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
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
public class StandardizeService {
    private final PetRepository firebirdPetRepository;
    private final PatientRepository postgresPatientRepository;

    @Transactional
    public Page<Patient> standardizeAndSaveFromFirebird(Hospital hospital, PageRequest pageRequest) {
        Page<Pair<Pet, Client>> petsAndClients = firebirdPetRepository.findAllPetsAndClients(pageRequest);
        log.info("original pet count: {}", petsAndClients.getTotalElements());

        List<Patient> patients = saveAllPatientsFromOrigin(
                hospital,
                petsAndClients.get()
                        .filter(p -> p.a.getOriginSpecies().toSpecies() != Species.ETC)
                        .toList()
        );

        return new PageImpl<>(patients, petsAndClients.getPageable(), petsAndClients.getTotalElements());
    }

    private List<Patient> saveAllPatientsFromOrigin(Hospital hospital, List<Pair<Pet, Client>> petsAndClients) {
        List<Patient> newPatients = petsAndClients.stream().map(p -> {
            Pet pet = p.a;
            Client client = p.b;

            log.info("original pet: {}", pet.toString());
            Patient patient = Patient.builder().name(pet.getName()).clientName(client.getName()).breed(pet.getBreed())
                    .birth(pet.getBirth()).address("").phone("").sex(pet.getSex()).originalId(pet.getId().toString())
                    .species(pet.getOriginSpecies().toSpecies()).hospital(hospital).build();
            log.info("convert patient: {}", patient.toString());
            return patient;
        }).toList();

        return postgresPatientRepository.saveAll(newPatients);
    }
}
