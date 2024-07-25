package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;

    public List<Prescription> saveAll(List<Prescription> prescriptions) {
        return prescriptionRepository.saveAll(prescriptions);
    }
}
