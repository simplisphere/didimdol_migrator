package com.simplisphere.didimdolstandardize.mssql.entities;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MsChartId implements Serializable {
    private Integer id;
    private Integer listOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsChartId msChartId = (MsChartId) o;
        return Objects.equals(id, msChartId.id) && Objects.equals(listOrder, msChartId.listOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, listOrder);
    }
}
