package com.simplisphere.didimdolstandardize.firebird.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "LABDATE")
@Comment("검사 날짜 정보")
public class SosulLabDate {

    @Id
    @Column(name = "LABDATE_ID", nullable = false)
    private Integer labDateId;

    @ManyToOne
    @JoinColumn(name = "PET_ID", nullable = false)
    private SosulPet pet;

    @Column(name = "LABDATE_TYPE", columnDefinition = "SMALLINT(5) default 1")
    private Short labDateType = 1;

    @Column(name = "LABDATE_TITLE", length = 30)
    private String labDateTitle;

    @Column(name = "LABDATE_DATE", nullable = false)
    private LocalDate labDateDate;

    @Column(name = "LABDATE_TIME", nullable = false)
    private LocalTime labDateTime;

    @Column(name = "LABPRODUCT_ID", columnDefinition = "INTEGER default -1")
    private Integer labProductId = -1;

    @Column(name = "SIGN_NAME", length = 32, nullable = false)
    private String signName;

    @Column(name = "LABDATE_MEMO", length = 1024)
    private String labDateMemo;
}