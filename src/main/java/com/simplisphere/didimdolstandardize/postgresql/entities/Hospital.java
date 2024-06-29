package com.simplisphere.didimdolstandardize.postgresql.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hospital_sequence")
    @SequenceGenerator(name = "hospital_sequence", sequenceName = "hospital_id_seq", allocationSize = 10, initialValue = 10000)
    private Long id;

    private String name;
    private String address;
    private String phone;
}
