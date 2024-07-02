package com.simplisphere.didimdolstandardize.firebird.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "LABRESULT")
@Comment("검사 항목 별 결과 수치")
public class SosulLabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LABRESULT_ID", nullable = false)
    private Integer labResultId;

    @ManyToOne
    @JoinColumn(name = "LABDATE_ID", nullable = false)
    private SosulLabDate labDate;

    @ManyToOne
    @JoinColumn(name = "LABITEM_ID", nullable = false)
    private SosulLabItem labItem;

    @Column(name = "LABRESULT_VALUE", length = 32)
    private String labResultValue;

    @Column(name = "LABRESULT_MIN", length = 20)
    private String labResultMin;

    @Column(name = "LABRESULT_MAX", length = 20)
    private String labResultMax;

    @Column(name = "LABRESULT_DESC", length = 64)
    private String labResultDesc;

    @Column(name = "SIGN_NAME", length = 32, nullable = false)
    private String signName;
}
