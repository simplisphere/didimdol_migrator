package com.simplisphere.didimdolstandardize.mssql.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MsPlanDetailId {
    private Integer id;
    private Integer listOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsPlanDetailId that = (MsPlanDetailId) o;
        return Objects.equals(id, that.id) && Objects.equals(listOrder, that.listOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, listOrder);
    }
}
