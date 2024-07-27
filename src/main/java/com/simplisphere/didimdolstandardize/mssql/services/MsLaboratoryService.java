package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.LabItemDto;
import com.simplisphere.didimdolstandardize.mssql.dtos.query.LabResultDto;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabProduct;
import com.simplisphere.didimdolstandardize.mssql.entities.MsLabReference;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsLabItemRepository;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsLabProductRepository;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsLabReferenceRepository;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsLabResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MsLaboratoryService {
    private final MsLabProductRepository msLabProductRepository;
    private final MsLabItemRepository msLabItemRepository;
    private final MsLabResultRepository msLabResultRepository;
    private final MsLabReferenceRepository msLabReferenceRepository;

    public Page<MsLabProduct> retrieveProducts(Pageable pageRequest) {
        return msLabProductRepository.findAllByIsLaboratory(pageRequest, true);
    }

    // retrieveItems() method
    public Page<LabItemDto> retrieveItems(Pageable pageRequest) {
        Page<Object[]> objects = msLabItemRepository.findLabItems(pageRequest);
        List<LabItemDto> itemDtos = objects.stream().map(result -> new LabItemDto(
                (Integer) result[0], // lbid
                (Integer) result[1],  // listOrder
                (String) result[2],  // name
                (String) result[3], // unit
                (Integer) result[4],  // productId
                (String) result[5]   // productCode
        )).collect(Collectors.toList());

        return new PageImpl<>(itemDtos, objects.getPageable(), objects.getTotalElements());
    }

    public Page<LabResultDto> getLaboratoryResults(Pageable pageable) {
        Page<Object[]> results = msLabResultRepository.findLabResults(pageable);
        List<LabResultDto> dtos = results.stream().map(this::convertToDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    public Page<MsLabReference> retrieveReferences(Pageable pageRequest, List<Integer> speciesIds) {
        return msLabReferenceRepository.findBySpeciesIdIn(pageRequest, speciesIds);
    }

    private LabResultDto convertToDto(Object[] result) {
        Timestamp legacyCreated = (Timestamp) result[6];
        LocalDateTime created = legacyCreated != null ? legacyCreated.toLocalDateTime() : null;
        return new LabResultDto(
                (Integer) result[0], // laboratory_type_id
                (Integer) result[1], // laboratory_item_id
                (Integer) result[2], // hlbvsid
                (Integer) result[3], // hlbplidx
                (Integer) result[4], // hlbidx
                (String) result[5],  // laboratory_result_result
                created,  // laboratory_result_created
                (Integer) result[7]  // pet_id
        );
    }
}
