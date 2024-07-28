package com.simplisphere.didimdolstandardize.postgresql.entities;

import com.simplisphere.didimdolstandardize.postgresql.MarkerType;
import com.simplisphere.didimdolstandardize.postgresql.Species;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

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
    private String code;
    private String description;
    @Enumerated(EnumType.STRING)
    private Species species;
    @Enumerated(EnumType.STRING)
    private MarkerType type;
    private String referenceUnit;
    private Float referenceMinimum;
    private Float referenceMaximum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardizeDiagnosisMarker that = (StandardizeDiagnosisMarker) o;
        return Objects.equals(name, that.name) && Objects.equals(code, that.code) && species == that.species && type == that.type && Objects.equals(diagnosis, that.diagnosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, species, type, diagnosis);
    }
}
