package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "hpl")
@IdClass(MsPlanDetailId.class)
@NamedEntityGraph(
        name = "MsPlanDetail.withFetchJoin",
        attributeNodes = {
                @NamedAttributeNode("pet"),
                @NamedAttributeNode("doctor"),
                @NamedAttributeNode("plan")
        }
)
public class MsPlanDetail {

    @Id
    @Column(name = "hplvsid")
    private Integer id;

    @Id
    @Column(name = "hplidx")
    private Integer listOrder;

    @Column(name = "hplrdt")
    private LocalDateTime createdAt;

    @Column(name = "hplcode", columnDefinition = "CHAR")
    private String code;

    @Column(name = "hpldesc")
    private String desc;

    @Column(name = "hpldcnt", columnDefinition = "smallint")
    private Integer dosagePerDay;

    @Column(name = "hplpcnt", columnDefinition = "smallint")
    private Integer days;

    @Column(name = "hplunitdosage")
    private String unitDosage;

    @ManyToOne
    @JoinColumn(name = "hplptid")
    private MsPet pet;

    @ManyToOne
    @JoinColumn(name = "hplremid")
    private MsDoctor doctor;

    @ManyToOne
    @JoinColumn(name = "hplplid")
    private MsPlan plan;
}