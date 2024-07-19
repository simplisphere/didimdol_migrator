package com.simplisphere.didimdolstandardize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DidimdolStandardizeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DidimdolStandardizeApplication.class, args);
    }
}
