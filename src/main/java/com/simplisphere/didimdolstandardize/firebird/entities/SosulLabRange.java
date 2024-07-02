package com.simplisphere.didimdolstandardize.firebird.entities;

import com.simplisphere.didimdolstandardize.firebird.SosulOriginSpecies;
import com.simplisphere.didimdolstandardize.firebird.SosulOriginalSpeciesConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "LABRANGE")
@Comment("검사 범위 정보")
public class SosulLabRange {

    @Id
    @Column(name = "LABRANGE_ID", nullable = false)
    private Integer id;

    @Column(name = "SPECIES_ID", columnDefinition = "integer")
    @Convert(converter = SosulOriginalSpeciesConverter.class)
    private SosulOriginSpecies species;

    @ManyToOne
    @JoinColumn(name = "LABITEM_ID", nullable = false)
    private SosulLabItem labItem;

    @Column(name = "LABRANGE_AGE_FROM", columnDefinition = "INTEGER default -9999")
    private Integer ageFrom = -9999;

    @Column(name = "LABRANGE_AGE_TO", columnDefinition = "INTEGER default 9999")
    private Integer ageTo = 9999;

    @Column(name = "LABRANGE_MIN", length = 20)
    private String min;

    @Column(name = "LABRANGE_MAX", length = 20)
    private String max;
}