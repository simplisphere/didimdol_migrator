package com.simplisphere.didimdolstandardize.mssql.repositories;

import com.simplisphere.didimdolstandardize.mssql.entities.MsPet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MsPetRepository extends JpaRepository<MsPet, Integer> {
    Optional<MsPet> findFirstByOrderByIdDesc();

//    @Query(value = "SELECT new com.simplisphere.didimdolstandardize.mssql.dtos.query.ClientPet(c.clid, c.cllname, c.claddr, c.claddr2, p.ptid, p.ptname, s.spdesc, b.brcode, sx.sxdesc, p.ptdob, p.ptclr, p.ptfdat, p.ptldat) " +
//            "FROM pt p " +
//            "JOIN cl c ON p.ptclid = c.clid " +
//            "JOIN sp s ON p.ptspid = s.spid " +
//            "JOIN br b ON p.ptbrid = b.brid " +
//            "JOIN sx ON p.ptsxid = sx.sxid", nativeQuery = true)
//    List<ClientPet> findClientPetDetails();

//    @Query(name = "MsPet.findClientPetDetails", nativeQuery = true)
//    List<ClientPet> findClientPetDetails();

    @Query(
            value = "SELECT c.clid AS clientId, c.cllname AS clientName, c.claddr AS clientAddress1, c.claddr2 AS clientAddress2, " +
                    "p.ptid AS petId, p.ptname AS petName, p.ptspid AS species, b.brcidesc AS breed, sx.sxcode AS sex, " +
                    "p.ptdob AS birth, p.ptclr AS color, p.ptfdat AS petFirstDate, p.ptldat AS petLastDate " +
                    "FROM pt p " +
                    "JOIN cl c ON p.ptclid = c.clid " +
                    "LEFT JOIN sp s ON p.ptspid = s.spid " +
                    "LEFT JOIN br b ON p.ptbrid = b.brid " +
                    "LEFT JOIN sx ON p.ptsxid = sx.sxid",
            countQuery = "SELECT COUNT(*) FROM pt p",
            nativeQuery = true
    )
    Page<Object[]> findClientPetDetails(Pageable pageable);
}