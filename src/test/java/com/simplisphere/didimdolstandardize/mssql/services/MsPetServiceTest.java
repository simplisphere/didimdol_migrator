package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.ClientPetDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
//@Transactional
//@Rollback
class MsPetServiceTest {

    @Autowired
    private MsPetService msPetService;

    @Test
    void testGetClientPetDetailsWithPageable() {
        Pageable pageable = PageRequest.of(0, 20000);
        Page<ClientPetDto> clientPetPage = msPetService.getClientPetDetails(pageable);

        clientPetPage.getContent().forEach(clientPetDto -> log.info("ClientPet: {}", clientPetDto));
        assertThat(clientPetPage).isNotNull();
        assertThat(clientPetPage.getContent()).isNotEmpty();
        assertThat(clientPetPage.getTotalElements()).isGreaterThan(0);
        assertThat(clientPetPage.getContent().get(0)).isInstanceOf(ClientPetDto.class);
    }
}