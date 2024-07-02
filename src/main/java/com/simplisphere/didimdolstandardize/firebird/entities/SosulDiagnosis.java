package com.simplisphere.didimdolstandardize.firebird.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "DX")
public class SosulDiagnosis {

    @Id
    @Column(name = "DX_ID", nullable = false)
    private Integer id;

    @Column(name = "DX_CODE", length = 16)
    private String code;

    @Column(name = "DX_NAME", length = 127)
    private String name;

    @Column(name = "DX_STDNAME", length = 127)
    private String stdName;
}
