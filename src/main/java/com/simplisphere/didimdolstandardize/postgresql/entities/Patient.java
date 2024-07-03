package com.simplisphere.didimdolstandardize.postgresql.entities;

import com.simplisphere.didimdolstandardize.postgresql.Species;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@ToString(exclude = {"vitals", "hospital", "charts"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Pet")
@Table(indexes = @Index(name = "idx_original_id", columnList = "original_id"))
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pet_gen")
    @SequenceGenerator(name = "pet_gen", sequenceName = "pet_id_seq", allocationSize = 10, initialValue = 10000)
    private Long id;

    private String name;
    private String clientName;
    private String address;
    private String phone;
    private LocalDate birth;
    private String sex;
    @Enumerated(EnumType.STRING)
    private Species species;
    private String breed;
    private String originalId;
    private LocalDateTime created;
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Vital> vitals;
}
