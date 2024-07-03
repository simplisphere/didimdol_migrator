package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = {"hospital", "patient"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Vital")
@Table(indexes = @Index(name = "idx_original_id", columnList = "original_id"))
public class Vital {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vital_id_seq")
    @SequenceGenerator(name = "vital_id_seq", sequenceName = "vital_id_seq", allocationSize = 1, initialValue = 10000)
    private Long id;

    private Float temperature;
    private Float pulse;
    // 분당 호흡수
    private Float respiratoryRate;
    private Float bloodPressure;
    private Float bodyWeight;
    private Float heartRate;
    private String doctor;
    private String originalId;
    private String originalPetId;
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
}
