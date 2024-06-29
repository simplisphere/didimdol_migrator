package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/standardize")
public class StandardizeController {
    private final StandardizeService standardizeService;
    private final HospitalRepository targetHospitalRepository;

    @PostMapping("patient")
    public ResponseEntity<?> standardizePatientInfo() {
        Hospital hospital = targetHospitalRepository.findByName("소설").orElseGet(() -> targetHospitalRepository.save(Hospital.builder()
                .name("소설")
                .address("경기도 성남시 분당구 판교")
                .phone("031-123-4567")
                .build()
        ));

        PageRequest pageRequest = PageRequest.of(0, 50, Sort.by("id").ascending());
        Page<Patient> newPatients = standardizeService.standardizeAndSaveFromFirebird(hospital, pageRequest);
        while (newPatients.hasNext()) {
            pageRequest = pageRequest.next();
            newPatients = standardizeService.standardizeAndSaveFromFirebird(hospital, pageRequest);
        }

        return ResponseEntity.ok(newPatients);
    }
}
