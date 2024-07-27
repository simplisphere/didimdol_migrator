package com.simplisphere.didimdolstandardize.mssql.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MsLabResultId {
    private Integer id;
    private Integer productIndex;
    private Integer itemIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsLabResultId that = (MsLabResultId) o;
        return Objects.equals(id, that.id) && Objects.equals(productIndex, that.productIndex) && Objects.equals(itemIndex, that.itemIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productIndex, itemIndex);
    }
}
