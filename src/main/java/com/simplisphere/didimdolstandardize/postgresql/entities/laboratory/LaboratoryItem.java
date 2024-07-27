package com.simplisphere.didimdolstandardize.postgresql.entities.laboratory;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = {"hospital", "type"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("검사 항목")
@SqlResultSetMapping(
        name = "LaboratoryItemWithTypeMapping",
        entities = {
                @EntityResult(
                        entityClass = LaboratoryItem.class,
                        fields = {
                                @FieldResult(name = "id", column = "li_id"),
                                @FieldResult(name = "name", column = "li_name"),
                                @FieldResult(name = "code", column = "li_code"),
                                @FieldResult(name = "abbreviation", column = "li_abbreviation"),
                                @FieldResult(name = "description", column = "li_description"),
                                @FieldResult(name = "unit", column = "li_unit"),
                                @FieldResult(name = "originalId", column = "li_original_id"),
                                @FieldResult(name = "orderIdx", column = "li_order_idx"),
                                @FieldResult(name = "hospital", column = "li_hospital_id"),
                                @FieldResult(name = "created", column = "li_created"),
                                @FieldResult(name = "updated", column = "li_updated"),
                                @FieldResult(name = "type", column = "li_laboratory_type_id")
                        }
                ),
                @EntityResult(
                        entityClass = LaboratoryType.class,
                        fields = {
                                @FieldResult(name = "id", column = "lt_id"),
                                @FieldResult(name = "name", column = "lt_name"),
                                @FieldResult(name = "abbreviation", column = "lt_abbreviation"),
                                @FieldResult(name = "description", column = "lt_description"),
                                @FieldResult(name = "hospital", column = "lt_hospital_id"),
                                @FieldResult(name = "originalId", column = "lt_original_id"),
                                @FieldResult(name = "created", column = "lt_created"),
                                @FieldResult(name = "updated", column = "lt_updated")
                        }
                )
        }
)
public class LaboratoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lab_item_gen")
    @SequenceGenerator(name = "lab_item_gen", sequenceName = "lab_item_id_seq", allocationSize = 1000, initialValue = 10000)
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
