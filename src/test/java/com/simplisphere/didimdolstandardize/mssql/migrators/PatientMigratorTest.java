package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.repositories.HospitalRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@SpringBootTest
class PatientMigratorTest {
    private static Hospital hospital;

    @Autowired
    private PatientMigrator patientMigrator;

    @BeforeAll
    static void setUp(@Autowired HospitalRepository repository) {
        hospital = repository.findByName("소설").orElseGet(() -> repository.save(Hospital.builder()
                .name("고강")
                .address("경기도 부천시")
                .phone("032-123-4567")
                .build()
        ));
    }

    @Test
    void convertPatient() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patients = patientMigrator.convertPatient(hospital, pageable);
        patients.stream().forEach(patient -> log.info("Patient: {}", patient));
    }
}