package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"originalDiagnoses"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Diagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standard_diagnosis_gen")
    @SequenceGenerator(name = "standard_diagnosis_gen", sequenceName = "standard_diagnosis_id_seq", allocationSize = 1, initialValue = 1000)
    private Long id;

    private String code;
    private String name;
    private String description;
    private LocalDateTime created;
    private LocalDateTime updated;
}