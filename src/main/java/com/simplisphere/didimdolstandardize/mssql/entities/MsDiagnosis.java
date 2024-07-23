package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "ss")
public class MsDiagnosis {
    @Id
    @Column(name = "ssid")
    private Integer id;

    @Column(name = "sscode", columnDefinition = "CHAR(20)")
    private String code;

    @Column(name = "sssmcode", columnDefinition = "CHAR(20)")
    private String smCode;

    @Column(name = "ssdesc")
    private String desc;

    @Column(name = "ssedesc")
    private String descEn;

    //    @Convert(converter = BooleanToTinyFloatConverter.class)
    @Column(name = "ssact", columnDefinition = "TINYINT")
    private Boolean active;
}
