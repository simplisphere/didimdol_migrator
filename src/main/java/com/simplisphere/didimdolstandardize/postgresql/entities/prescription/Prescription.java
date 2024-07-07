package com.simplisphere.didimdolstandardize.postgresql.entities.prescription;

import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = {"patient", "hospital", "chart", "medicine"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prescription_gen")
    @SequenceGenerator(name = "prescription_gen", sequenceName = "prescription_id_seq", allocationSize = 1, initialValue = 100000)
    private Long id;

    private String code = "";
    private String name = "";
    // 복용일수
    private Integer days = 0;
    // 하루 복용 횟수
    private Integer dosePerDay = 0;
    // 총 복용량
    private Integer total = 0;
    // 회당 복용량
//    private Float qty = 0.0f;
    // 단위
    private String unit = "";
    private String doctor = "";

    private String originalId;

    private LocalDateTime created;
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_id")
    private Chart chart;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;
}
