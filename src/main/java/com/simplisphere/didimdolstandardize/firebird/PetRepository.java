package com.simplisphere.didimdolstandardize.firebird;

import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Integer> {
    @Query("SELECT new org.antlr.v4.runtime.misc.Pair(p, c) from Pet p join Client c on p.familyId = c.familyId")
    List<Pair<Pet, Client>> findAllPetsAndClients();

    @Query("SELECT new org.antlr.v4.runtime.misc.Pair(p, c) from Pet p join Client c on p.familyId = c.familyId")
    Page<Pair<Pet, Client>> findAllPetsAndClients(Pageable pageable);
}
