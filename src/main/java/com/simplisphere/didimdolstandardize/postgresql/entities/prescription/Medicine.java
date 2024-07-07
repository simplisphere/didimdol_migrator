package com.simplisphere.didimdolstandardize.postgresql.entities.prescription;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medicine_gen")
    @SequenceGenerator(name = "medicine_gen", sequenceName = "medicine_id_seq", allocationSize = 1, initialValue = 10000)
    private Long id;

    private String name;
    private String description;
}
