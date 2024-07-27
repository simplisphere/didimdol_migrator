package com.simplisphere.didimdolstandardize.mssql;

import com.simplisphere.didimdolstandardize.postgresql.Species;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Slf4j
@Getter
@AllArgsConstructor
public enum MsOriginSpecies {
    Canine(24, Species.CANINE),
    Feline(29, Species.FELINE),
    Rabbit(30, Species.ETC),
    Hedgehog(51, Species.ETC),
    Ferret(50, Species.ETC),
    Hamster(52, Species.ETC),
    GuineaPig(47, Species.ETC),
    MiniPig(48, Species.ETC),
    Turtle(53, Species.ETC),
    Avian(31, Species.ETC),
    Rodent(32, Species.ETC),
    Reptile(33, Species.ETC),
    Gerbil(49, Species.ETC),
    Chinchilla(54, Species.ETC),
    Amphibians(55, Species.ETC),
    Arthropods(56, Species.ETC),
    기타(21, Species.ETC),
    Etc(45, Species.ETC);

    private final int id;
    private final Species species;

    public static MsOriginSpecies of(Integer speciesId) {
        return Stream.of(MsOriginSpecies.values())
                .filter(p -> p.getId() == speciesId)
                .findFirst()
                .orElse(MsOriginSpecies.Etc);
    }

    public Species toSpecies() {
        return this.species;
    }
}
