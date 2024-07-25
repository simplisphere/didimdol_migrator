package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.repositories.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;

    public Medicine findOrCreate(Medicine medicine) {
        return medicineRepository.findByName(medicine.getName())
                .orElseGet(() -> medicineRepository.save(medicine));
    }
}
