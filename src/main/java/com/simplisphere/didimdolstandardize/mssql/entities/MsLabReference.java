package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "lr")
@IdClass(MsLabReferenceId.class)
public class MsLabReference {
    @Id
    @Column(name = "lrlbid")
    private Integer itemId;

    @Id
    @Column(name = "lrspid")
    private Integer speciesId;

    @Column(name = "lrmin", columnDefinition = "char")
    private String min;

    @Column(name = "lrmax", columnDefinition = "char")
    private String max;

    public String getOriginalId() {
        return itemId.toString() + speciesId.toString();
    }
}
