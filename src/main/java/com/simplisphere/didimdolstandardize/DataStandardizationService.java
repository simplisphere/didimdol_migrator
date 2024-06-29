package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.firebird.FirebirdDataEntity;
import com.simplisphere.didimdolstandardize.firebird.FirebirdDataRepository;
import com.simplisphere.didimdolstandardize.postgresql.PostgreSQLDataEntity;
import com.simplisphere.didimdolstandardize.postgresql.PostgreSQLDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataStandardizationService {
    private static final Logger logger = LoggerFactory.getLogger(DataStandardizationService.class);
    private final FirebirdDataRepository firebirdDataRepository;
    private final PostgreSQLDataRepository postgresqlDataRepository;

    public void standardizeAndSaveFromFirebird() {
//        List<FirebirdDataEntity> firebirdData = firebirdDataRepository.findTop3();
        FirebirdDataEntity firebirdData = firebirdDataRepository.findFirstById("298552");
        logger.info(firebirdData.toString());
        List<PostgreSQLDataEntity> datas = postgresqlDataRepository.findAll();
        logger.info(datas.toString());
//        String standardizedData = standardize(firebirdData.getData());

//        PostgreSQLDataEntity postgresqlData = new PostgreSQLDataEntity();
//        postgresqlData.setData(standardizedData);
//        postgresqlDataRepository.save(postgresqlData);
    }

    private String standardize(String rawData) {
        // 예시 표준화 로직
        return rawData.trim().toLowerCase();
    }
}
