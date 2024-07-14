package com.simplisphere.didimdolstandardize;

public interface Migrator {
    void beforeMigrate();

    void migrate();

    void afterMigrate();
}
