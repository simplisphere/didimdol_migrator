package com.simplisphere.didimdolstandardize.postgresql.entities.laboratory;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
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
@Comment("검사 항목 별 결과 수치")
public class LaboratoryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lab_result_gen")
    @SequenceGenerator(name = "lab_result_gen", sequenceName = "lab_result_id_seq", allocationSize = 1, initialValue = 10000)
    Long id;

    private String description;
    private String result;
    @Comment("검사 일자")
    private LocalDateTime created;
    private String originalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_item_id")
    private LaboratoryItem laboratoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Patient patient;
}
