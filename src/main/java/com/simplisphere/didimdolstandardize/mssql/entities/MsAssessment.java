package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity(name = "pb")
@NamedEntityGraph(
        name = "MsAssessment.withFetchJoin",
        attributeNodes = {
                @NamedAttributeNode("pet"),
                @NamedAttributeNode("assessmentDoctor"),
                @NamedAttributeNode("stageDoctor")
        }
)
public class MsAssessment {
    @Id
    @Column(name = "pbid")
    private Integer id;

    @Column(name = "pbrdt")
    private LocalDateTime createdAt;

    @Column(name = "pbvsid")
    private Integer chartId;

    @Column(name = "pbhsscode", columnDefinition = "CHAR")
    private String code;

    @Column(name = "pbdesc")
    private String name;

    @Column(name = "pbmark", columnDefinition = "tinyint")
    private Short stage;

    @Column(name = "pbmarkdt")
    private LocalDateTime stageDate;

    @ManyToOne()
    @JoinColumn(name = "pbptid", nullable = false)
    private MsPet pet;

    @ManyToOne()
    @JoinColumn(name = "pbremid", nullable = false)
    private MsDoctor assessmentDoctor;

    @ManyToOne()
    @JoinColumn(name = "pbmarkemid", nullable = false)
    private MsDoctor stageDoctor;


}
