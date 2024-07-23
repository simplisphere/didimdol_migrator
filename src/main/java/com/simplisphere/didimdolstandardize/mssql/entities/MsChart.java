package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@Entity(name = "hse")
@IdClass(MsChartId.class)
@NamedEntityGraph(
        name = "MsChart.withFetchJoin",
        attributeNodes = {
                @NamedAttributeNode("pet"),
                @NamedAttributeNode("doctor")
        }
)
public class MsChart {
    @Id
    @Column(name = "hsevsid")
    private Integer id;

    @Id
    @Column(name = "hselstord")
    private Integer listOrder;

    @Column(name = "hsecont", columnDefinition = "TEXT")
    private String content1;

    @Column(name = "hsetxtcont", columnDefinition = "TEXT")
    private String content2;

    @Column(name = "hserdt")
    private LocalDateTime createdAt;

    @Column(name = "hsemdt")
    private LocalDateTime updatedAt;

    @ManyToOne()
    @JoinColumn(name = "hseptid")
    private MsPet pet;

    @ManyToOne()
    @JoinColumn(name = "hseremid")
    private MsDoctor doctor;
}
