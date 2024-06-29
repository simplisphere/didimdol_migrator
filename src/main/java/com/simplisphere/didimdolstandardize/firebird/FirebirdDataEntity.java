package com.simplisphere.didimdolstandardize.firebird;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "ASSESSMENT")
public class FirebirdDataEntity {
    @Id
    @Column(name = "ASSESSMENT_ID")
    private String id;

    @Column(name = "ASSESSMENT_NAME")
    private String name;
}
