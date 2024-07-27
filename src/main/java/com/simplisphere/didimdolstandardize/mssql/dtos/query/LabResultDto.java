package com.simplisphere.didimdolstandardize.mssql.dtos.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LabResultDto {
    private Integer laboratoryTypeId;
    private Integer laboratoryItemId;
    private Integer hlbvsid;
    private Integer hlbplidx;
    private Integer hlbidx;
    private String laboratoryResultResult;
    private LocalDateTime laboratoryResultCreated;
    private Integer petId;

    public String getOriginalId() {
        return this.hlbvsid + StringUtils.leftPad(this.hlbplidx.toString(), 2, "0") + StringUtils.leftPad(this.hlbidx.toString(), 2, "0");
    }
}
