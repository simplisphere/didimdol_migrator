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
@Table(name = "lb")
public class MsLabItem {
    @Id
    @Column(name = "lbid")
    private Integer id;

    @Column(name = "lbdesc")
    private String name;

    @Column(name = "lbunit", columnDefinition = "char")
    private String unit;
}
