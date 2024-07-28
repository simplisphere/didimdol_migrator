package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString(exclude = {"hospitalDiagnoses"})
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

    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL)
    private List<HospitalDiagnosis> hospitalDiagnoses;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Diagnosis diagnosis = (Diagnosis) o;
        return Objects.equals(name, diagnosis.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}