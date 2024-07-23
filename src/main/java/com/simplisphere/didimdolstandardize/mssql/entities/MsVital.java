package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity(name = "hcl")
@NamedEntityGraph(
        name = "MsVital.withFetchJoin",
        attributeNodes = {
                @NamedAttributeNode("pet"),
                @NamedAttributeNode("doctor")
        }
)
public class MsVital {
    @Id
    @Column(name = "hclid")
    private Integer id;

    @Column(name = "hclrdt")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "hclptid")
    private MsPet pet;

    @ManyToOne()
    @JoinColumn(name = "hclremid")
    private MsDoctor doctor;
}
