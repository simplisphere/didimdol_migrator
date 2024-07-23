package com.simplisphere.didimdolstandardize.mssql.dtos.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VitalDto {
    private Integer vitalId;
    private Integer petId;
    private LocalDateTime createdAt;
    private String bodyWeight;
    private String bodyTemperature;
    private String bloodPressure;
    private String doctor;
}
