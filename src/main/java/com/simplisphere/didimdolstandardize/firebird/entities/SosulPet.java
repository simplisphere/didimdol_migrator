package com.simplisphere.didimdolstandardize.firebird.entities;

import com.simplisphere.didimdolstandardize.firebird.SosulOriginSpecies;
import com.simplisphere.didimdolstandardize.firebird.SosulOriginalSpeciesConverter;
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
@Entity(name = "PET")
public class SosulPet {
    @Id
    @Column(name = "PET_ID")
    private Integer id;
    @Column(name = "PET_NAME")
    private String name;
    @Column(name = "SPECIES_ID", columnDefinition = "integer")
    @Convert(converter = SosulOriginalSpeciesConverter.class)
    private SosulOriginSpecies sosulOriginSpecies;
    @Column(name = "PET_BREED")
    private String breed;
    @Column(name = "PET_SEX")
    private String sex;
    @Column(name = "PET_BIRTH")
    private LocalDate birth;
    @Column(name = "FAMILY_ID")
    private Integer familyId;
}
