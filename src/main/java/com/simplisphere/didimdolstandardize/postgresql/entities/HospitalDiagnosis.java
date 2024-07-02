package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HospitalDiagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hospital_dx_gen")
    @SequenceGenerator(name = "hospital_dx_gen", sequenceName = "hospital_dx_id_seq", allocationSize = 1, initialValue = 202400000)
    private Long id;

    private String code;
    private String name;
    private String description;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String originalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;
}