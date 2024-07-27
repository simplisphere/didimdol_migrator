package com.simplisphere.didimdolstandardize.mssql.dtos.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LabItemDto {
    private Integer id;
    private Integer listOrder;
    private String name;
    private String unit;
    private Integer productId;
    private String productCode;
}
