package com.simplisphere.didimdolstandardize.firebird;

import com.simplisphere.didimdolstandardize.postgresql.Species;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Slf4j
@Getter
@AllArgsConstructor
public enum SosulOriginSpecies {
    Canine(1, Species.CANINE),
    Feline(2, Species.FELINE),
    Rabbit(3, Species.ETC),
    Equine(4, Species.ETC),
    Hedgehog(5, Species.ETC),
    Ferret(6, Species.ETC),
    Hamster(7, Species.ETC),
    GuineaPig(8, Species.ETC),
    Iguana(9, Species.ETC),
    Lizard(10, Species.ETC),
    Snake(11, Species.ETC),
    Turtle(12, Species.ETC),
    Avian(13, Species.ETC),
    Rodent(14, Species.ETC),
    Reptile(15, Species.ETC),
    Bovine(16, Species.ETC),
    Aquatic(17, Species.ETC),
    Ovine(18, Species.ETC),
    Swine(19, Species.ETC),
    Cervine(20, Species.ETC),
    Etc(21, Species.ETC);

    private final int id;
    private final Species species;

    public static SosulOriginSpecies of(Integer speciesId) {
        return Stream.of(SosulOriginSpecies.values())
                .filter(p -> p.getId() == speciesId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Species toSpecies() {
        return this.species;
    }
}
