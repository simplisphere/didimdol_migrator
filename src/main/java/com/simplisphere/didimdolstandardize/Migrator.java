package com.simplisphere.didimdolstandardize;

import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;

import java.util.concurrent.CompletableFuture;

public interface Migrator {
    void beforeMigrate();

    void migrate();

    void afterMigrate();

    CompletableFuture<Void> migratePatient(Hospital hospital);

    CompletableFuture<Void> migrateChart(Hospital hospital);

    CompletableFuture<Void> migrateAssessment();

    CompletableFuture<Void> migrateDiagnosis(Hospital hospital);

    CompletableFuture<Void> migrateLaboratory(Hospital hospital);

    CompletableFuture<Void> migrateLaboratoryType(Hospital hospital);

    CompletableFuture<Void> migrateLaboratoryItem(Hospital hospital);

    CompletableFuture<Void> migrateLaboratoryReference(Hospital hospital);

    CompletableFuture<Void> migrateLaboratoryResult(Hospital hospital);

    CompletableFuture<Void> migratePrescription(Hospital hospital);

    CompletableFuture<Void> migrateVital(Hospital hospital);
}
