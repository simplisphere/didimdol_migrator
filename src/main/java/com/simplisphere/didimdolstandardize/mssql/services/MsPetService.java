package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.ClientPetDto;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsPetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MsPetService {
    private final MsPetRepository msPetRepository;

    public Page<ClientPetDto> getClientPetDetails(Pageable pageable) {
        Page<Object[]> results = msPetRepository.findClientPetDetails(pageable);
        List<ClientPetDto> clientPetDtos = results.stream().map(result -> new ClientPetDto(
                ((Number) result[0]).longValue(), // clientId
                (String) result[1],               // clientName
                (String) result[2],               // clientAddress1
                (String) result[3],               // clientAddress2
                ((Number) result[4]).longValue(), // petId
                (String) result[5],               // petName
                (String) result[6],               // species
                (String) result[7],               // breed
                (String) result[8],               // sex
                toLocalDate((java.sql.Timestamp) result[9]), // birth
                (String) result[10],              // color
                toLocalDate((java.sql.Timestamp) result[11]), // petFirstDate
                toLocalDate((java.sql.Timestamp) result[12])  // petLastDate
        )).collect(Collectors.toList());

        return new PageImpl<>(clientPetDtos, pageable, results.getTotalElements());
    }

    public Long count() {
        return msPetRepository.count();
    }

    private LocalDate toLocalDate(java.sql.Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime().toLocalDate() : null;
    }
}
