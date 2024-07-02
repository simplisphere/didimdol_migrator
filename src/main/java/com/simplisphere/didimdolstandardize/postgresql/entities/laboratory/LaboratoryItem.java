package com.simplisphere.didimdolstandardize.postgresql.entities.laboratory;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = "type")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("검사 항목")
public class LaboratoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lab_item_gen")
    @SequenceGenerator(name = "lab_item_gen", sequenceName = "lab_item_id_seq", allocationSize = 1, initialValue = 10000)
    Long id;

    private String name;
    private String code;
    private String abbreviation;
    private String description;
    private String unit;
    private String originalId;
    private Integer orderIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    private LocalDateTime created;
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_type_id")
    private LaboratoryType type;
}
