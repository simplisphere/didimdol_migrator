package com.simplisphere.didimdolstandardize.postgresql.entities;

import com.simplisphere.didimdolstandardize.postgresql.MarkerType;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString(exclude = {"diagnosis"})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class StandardizeDiagnosisMarker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // type.PRESCRIPTION: Medicine Name
    // type.LABORATORY: Laboratory Item Name
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Species species;
    @Enumerated(EnumType.STRING)
    private MarkerType type;
    private String referenceUnit;
    private Float referenceMinimum;
    private Float referenceMaximum;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "diagnosis_id")
//    private Diagnosis diagnosis;
}
