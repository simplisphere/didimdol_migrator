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
public class MsPlan {

    @Id
    @Column(name = "plid")
    private Integer id;

    @Column(name = "plyplid")
    private Integer type;
}