package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "pl")
public class MsLabProduct {
    @Id
    @Column(name = "plid")
    private Integer id;

    @Column(name = "pldesc")
    private String name;

    @Column(name = "pllab", columnDefinition = "tinyint")
    private Boolean isLaboratory;
}
