package com.simplisphere.didimdolstandardize.firebird;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pet {
    @Id
    @Column(name = "PET_ID")
    private Integer id;
    @Column(name = "PET_NAME")
    private String name;
    @Column(name = "SPECIES_ID", columnDefinition = "integer")
    @Convert(converter = OriginalSpeciesConverter.class)
    private OriginSpecies originSpecies;
    @Column(name = "PET_BREED")
    private String breed;
    @Column(name = "PET_SEX")
    private String sex;
    @Column(name = "PET_BIRTH")
    private LocalDate birth;
    @Column(name = "FAMILY_ID")
    private Integer familyId;
}
