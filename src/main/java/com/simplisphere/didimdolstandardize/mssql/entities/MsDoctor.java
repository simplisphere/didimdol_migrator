package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity(name = "em")
public class MsDoctor {
    @Id
    @Column(name = "emid")
    private Integer id;

    @Column(name = "emname")
    private String name;
}
