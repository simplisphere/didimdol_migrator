package com.simplisphere.didimdolstandardize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class StandardizedRunner implements ApplicationRunner {

    private final Migrator migrator;
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Didimdol Standardize start.");
        log.info("zone: {}", ZoneId.systemDefault());

        Instant start = Instant.now();

        log.info("start migration");

        migrator.beforeMigrate();
        migrator.migrate();
        migrator.afterMigrate();

        Instant end = Instant.now();
        log.info("Start time: {}", LocalDateTime.ofInstant(start, ZoneId.systemDefault()));
        log.info("End time: {}", LocalDateTime.ofInstant(end, ZoneId.systemDefault()));
        Duration timeElapsed = Duration.between(start, end);
        log.info("Time elapsed: {}s", timeElapsed.toSeconds());

        log.info("Didimdol Standardize is finished.");

        // 작업 완료 후 애플리케이션 종료
        SpringApplication.exit(applicationContext, () -> 0);
    }
}
