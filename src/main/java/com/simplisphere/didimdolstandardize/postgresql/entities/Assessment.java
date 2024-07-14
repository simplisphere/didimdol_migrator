package com.simplisphere.didimdolstandardize.postgresql.entities;

import com.simplisphere.didimdolstandardize.postgresql.AssessmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString(exclude = {"chart", "patient"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_gen")
    @SequenceGenerator(name = "assessment_gen", sequenceName = "assessment_id_seq", allocationSize = 1000, initialValue = 100000)
    private Long id;
    private String name;
    @Enumerated(EnumType.ORDINAL)
    private AssessmentStatus status;
    private String originalId;
    private String originalPetId;
    private String doctor;

    @ManyToOne
    @JoinColumn(name = "chart_id", nullable = true)
    private Chart chart;

    @ManyToOne
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
}