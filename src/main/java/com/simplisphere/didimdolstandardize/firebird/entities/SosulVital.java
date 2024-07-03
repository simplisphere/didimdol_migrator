package com.simplisphere.didimdolstandardize.firebird.entities;

import com.simplisphere.didimdolstandardize.firebird.SosulPet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "VITAL")
@Comment("생체 정보")
public class SosulVital {

    @Id
    @Column(name = "VITAL_ID", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "PET_ID", nullable = false)
    private SosulPet pet;

    @Column(name = "VITAL_DATE", nullable = false)
    private LocalDate date;

    @Column(name = "VITAL_TIME", nullable = false)
    private LocalTime time;

    @Column(name = "VITAL_BW", columnDefinition = "FLOAT(7)")
    private Float bw;

    @Column(name = "VITAL_BT", columnDefinition = "FLOAT(7)")
    private Float bt;

    @Column(name = "VITAL_BP", columnDefinition = "FLOAT(7)")
    private Float bp;

    @Column(name = "VITAL_HR", columnDefinition = "FLOAT(7)")
    private Float hr;

    @Column(name = "VITAL_MEMO")
    private String memo;

    @Column(name = "SIGN_NAME", length = 32, nullable = false)
    private String signName;

    @Column(name = "VITAL_RR", columnDefinition = "FLOAT(7) default 0")
    private Float rr = 0f;

    @Column(name = "VITAL_BP2", columnDefinition = "FLOAT(7) default 0")
    private Float bp2 = 0f;
}