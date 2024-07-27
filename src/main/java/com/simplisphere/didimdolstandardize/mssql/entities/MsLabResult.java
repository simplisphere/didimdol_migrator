package com.simplisphere.didimdolstandardize.mssql.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "hlb")
@IdClass(MsLabResultId.class)
public class MsLabResult {
    @Id
    @Column(name = "hlbvsid")
    private Integer id;

    @Id
    @Column(name = "hlbplidx")
    private Integer productIndex;

    @Id
    @Column(name = "hlbidx")
    private Integer itemIndex;

    @Column(name = "hlbresult", columnDefinition = "char")
    private String result;

    @Column(name = "hlbindt")
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "hlbptid")
    private MsPet pet;
}
