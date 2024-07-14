package com.simplisphere.didimdolstandardize.postgresql.entities.laboratory;

import com.simplisphere.didimdolstandardize.postgresql.Species;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@ToString(exclude = "laboratoryItem")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("검사 항목 별 정상 수치 정보")
public class LaboratoryReference {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lab_reference_gen")
    @SequenceGenerator(name = "lab_reference_gen", sequenceName = "lab_reference_id_seq", allocationSize = 1000, initialValue = 10000)
    Long id;

    @Enumerated(EnumType.STRING)
    private Species species;
    private String unit;
    private Integer fromAge;
    private Integer toAge;
    private String minReferenceRange;
    private String maxReferenceRange;
    private String originalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_item_id")
    private LaboratoryItem laboratoryItem;
}
