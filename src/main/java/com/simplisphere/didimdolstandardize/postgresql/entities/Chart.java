package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Getter
//@ToString(exclude = {"assessments"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(indexes = @Index(name = "idx_original_id", columnList = "original_id"))
public class Chart {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chart_gen")
    @SequenceGenerator(name = "chart_gen", sequenceName = "chart_id_seq", allocationSize = 1, initialValue = 100000)
    private Long id;

    private LocalDateTime chartDate;

    @Column(columnDefinition = "TEXT")
    private String subject;
    private String objective;
    private String originalId;
    private String originalPetId;
    private String cc;
    private String doctor;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

//    @OneToMany(mappedBy = "chart", cascade = CascadeType.ALL)
//    private List<Assessment> assessments = new ArrayList<>();
}
