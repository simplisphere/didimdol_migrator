package com.simplisphere.didimdolstandardize.mssql.services;

import com.simplisphere.didimdolstandardize.mssql.entities.MsDiagnosis;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MsDiagnosisServiceTest {

    @Autowired
    private MsDiagnosisService msDiagnosisService;

    @Test
    void retrieveActiveDiagnosesReturnsNotEmptyListWhenNoActiveDiagnoses() {
        List<MsDiagnosis> result = msDiagnosisService.retrieveActiveDiagnoses();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testRetrieveActiveDiagnoses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MsDiagnosis> page = msDiagnosisService.retrieveActiveDiagnoses(pageable);

        assertNotNull(page);
        assertEquals(10, page.getContent().size());
    }
}