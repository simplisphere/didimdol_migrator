package com.simplisphere.didimdolstandardize.firebird.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CHART")
@NamedEntityGraph(name = "SosulChart.sosulPet", attributeNodes = @NamedAttributeNode("sosulPet"))
public class SosulChart {
    @Id
    @Column(name = "CHART_ID")
    Integer id;

    @Column(name = "CHART_TYPE")
    String type;

    @Column(name = "CHART_DATE")
    LocalDate date;

    @Column(name = "CHART_TIME")
    LocalTime time;

    @Column(name = "SIGN_NAME")
    String sign;

    @Column(name = "CHART_MEMO")
    String memo;

    @Column(name = "CHART_CC")
    String cc;

    @ManyToOne
    @JoinColumn(name = "PET_ID")
    private SosulPet sosulPet;
}
