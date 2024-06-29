package com.simplisphere.didimdolstandardize.firebird;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FirebirdDataRepository extends JpaRepository<FirebirdDataEntity, String> {
//    List<FirebirdDataEntity> findTop3();
    FirebirdDataEntity findFirstById(String id);
}
