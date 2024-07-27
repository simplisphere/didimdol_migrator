package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.MsOriginSpecies;
import com.simplisphere.didimdolstandardize.mssql.dtos.query.ClientPetDto;
import com.simplisphere.didimdolstandardize.mssql.services.MsPetService;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientMigrator {
    private static final Map<String, String> sexMap = new HashMap<>();
    private static final Map<String, String> breedMap = new HashMap<>();

    static {
        sexMap.put("F", "Female");
        sexMap.put("un", "Spayed female");
        sexMap.put("SF", "Spayed female");
        sexMap.put("FS", "Spayed female");
        sexMap.put("MN", "Castrated male");
        sexMap.put("M", "Male");
        sexMap.put("U", "None");
    }

    private final MsPetService msPetService;

    public static String convertBreed(String breedCode) {
        if (breedCode == null) {
            return "None";
        }
        return breedCode.trim();
    }

    public static String convertSex(String sex) {
        if (sex == null) {
            return "None";
        }
        return sexMap.getOrDefault(sex.trim(), "None");
    }

    public Page<Patient> convertPatient(Hospital hospital, Pageable pageRequest) {
        Page<ClientPetDto> legacyClientPet = msPetService.getClientPetDetails(pageRequest);
        List<Patient> patients = legacyClientPet.stream().parallel().map(clientPetDto -> {
            log.trace("ClientPet: {}", clientPetDto.toString());
            String address = clientPetDto.getClientAddress1() + " " + clientPetDto.getClientAddress2();
            LocalDateTime petFirstDate = Optional.ofNullable(clientPetDto.getPetFirstDate())
                    .map(LocalDate::atStartOfDay)
                    .orElse(null);
            return Patient.builder()
                    .name(clientPetDto.getPetName().trim())
                    .clientName(clientPetDto.getClientName().trim())
                    .breed(convertBreed(clientPetDto.getBreed()))
                    .birth(clientPetDto.getBirth())
                    .address(address.trim())
                    .phone("")
                    .sex(convertSex(clientPetDto.getSex()))
                    .originalId(clientPetDto.getPetId().toString())
                    .species(MsOriginSpecies.of(clientPetDto.getSpecies()).toSpecies())
                    .hospital(hospital)
                    .created(petFirstDate)
                    .updated(petFirstDate)
                    .build();
        }).toList();

        return new PageImpl<>(patients, legacyClientPet.getPageable(), legacyClientPet.getTotalElements());
    }
}
