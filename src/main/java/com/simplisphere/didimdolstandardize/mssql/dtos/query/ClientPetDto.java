package com.simplisphere.didimdolstandardize.mssql.dtos.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
public class ClientPetDto {
    private Long clientId;
    private String clientName;
    private String clientAddress1;
    private String clientAddress2;
    private Long petId;
    private String petName;
    private String species;
    private String breed;
    private String sex;
    private LocalDate birth;
    private String color;
    private LocalDate petFirstDate;
    private LocalDate petLastDate;
}
