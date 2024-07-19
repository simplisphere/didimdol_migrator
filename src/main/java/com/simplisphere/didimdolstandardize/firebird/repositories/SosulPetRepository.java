package com.simplisphere.didimdolstandardize.firebird.repositories;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulClient;
import com.simplisphere.didimdolstandardize.firebird.entities.SosulPet;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SosulPetRepository extends JpaRepository<SosulPet, Integer> {
    @Query("SELECT new org.antlr.v4.runtime.misc.Pair(p, c) from PET p join CLIENT c on p.familyId = c.familyId")
    Page<Pair<SosulPet, SosulClient>> findAllPetsAndClients(Pageable pageable);
}
