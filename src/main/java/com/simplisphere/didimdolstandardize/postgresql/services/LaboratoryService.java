package com.simplisphere.didimdolstandardize.postgresql.services;

import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryItem;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryReference;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryResult;
import com.simplisphere.didimdolstandardize.postgresql.entities.laboratory.LaboratoryType;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryItemRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryReferenceRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryResultRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.LaboratoryTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LaboratoryService {
    private final LaboratoryTypeRepository laboratoryTypeRepository;
    private final LaboratoryItemRepository laboratoryItemRepository;
    private final LaboratoryReferenceRepository laboratoryReferenceRepository;
    private final LaboratoryResultRepository laboratoryResultRepository;

    public List<LaboratoryType> saveLaboratoryTypes(List<LaboratoryType> laboratoryTypes) {
        return laboratoryTypeRepository.saveAll(laboratoryTypes);
    }

    public List<LaboratoryItem> saveLaboratoryItems(List<LaboratoryItem> laboratoryItems) {
        return laboratoryItemRepository.saveAll(laboratoryItems);
    }

    public List<LaboratoryReference> saveLaboratoryReferences(List<LaboratoryReference> laboratoryReferences) {
        return laboratoryReferenceRepository.saveAll(laboratoryReferences);
    }

    public List<LaboratoryResult> saveLaboratoryResults(List<LaboratoryResult> laboratoryResults) {
        return laboratoryResultRepository.saveAll(laboratoryResults);
    }
}
