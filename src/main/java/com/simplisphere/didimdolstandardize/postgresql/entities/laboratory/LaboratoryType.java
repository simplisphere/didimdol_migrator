package com.simplisphere.didimdolstandardize.postgresql.entities.laboratory;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("검사 종류/장비")
public class LaboratoryType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lab_type_gen")
    @SequenceGenerator(name = "lab_type_gen", sequenceName = "lab_type_id_seq", allocationSize = 1, initialValue = 10000)
    Long id;

    private String name;
    private String abbreviation;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    private String originalId;
    private LocalDateTime created;
    private LocalDateTime updated;
}
