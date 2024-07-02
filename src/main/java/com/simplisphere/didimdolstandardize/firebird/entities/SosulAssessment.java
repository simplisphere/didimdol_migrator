package com.simplisphere.didimdolstandardize.firebird.entities;

import com.simplisphere.didimdolstandardize.firebird.SosulChart;
import com.simplisphere.didimdolstandardize.firebird.SosulPet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "ASSESSMENT")
@Comment("평가 정보")
public class SosulAssessment {
    @Id
    @Column(name = "ASSESSMENT_ID", nullable = false)
    private Integer id;

    @Column(name = "ASSESSMENT_TYPE", nullable = false)
    private Integer type;

    @Column(name = "ASSESSMENT_CODE", length = 16)
    private String code;

    @Column(name = "ASSESSMENT_NAME", length = 127)
    private String name;

    @Column(name = "ASSESSMENT_FINALDX")
    private LocalDate finalDx;

    @Column(name = "ASSESSMENT_COMPLETED")
    private LocalDate completed;

    @Column(name = "SIGN_NAME", length = 32, nullable = false)
    private String sign;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "MODIFIED")
    private LocalDateTime modified;

    @OneToOne
    @JoinColumn(name = "PET_ID")
    private SosulPet pet;

    @OneToOne
    @JoinColumn(name = "CHART_ID")
    private SosulChart chart;
}