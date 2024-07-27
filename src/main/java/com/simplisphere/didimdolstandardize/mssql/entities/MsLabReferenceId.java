package com.simplisphere.didimdolstandardize.mssql.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MsLabReferenceId implements Serializable {
    private Integer itemId;
    private Integer speciesId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsLabReferenceId that = (MsLabReferenceId) o;
        return Objects.equals(itemId, that.itemId) && Objects.equals(speciesId, that.speciesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, speciesId);
    }
}
