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
@Table(name = "LABITEM")
@Comment("검사 항목 정보")
public class SosulLabItem {

    @Id
    @Column(name = "LABITEM_ID", nullable = false)
    private Integer labItemId;

    @Column(name = "LABITEM_CODE", length = 32)
    private String labItemCode;

    @Column(name = "LABITEM_ABBR", length = 64)
    private String labItemAbbr;

    @Column(name = "LABITEM_NAME", length = 100)
    private String labItemName;

    @Column(name = "LABITEM_UNIT", length = 32)
    private String labItemUnit;

    @ManyToOne
    @JoinColumn(name = "LABPRODUCT_ID", nullable = false)
    private SosulLabProduct labProduct;

    @Column(name = "LABITEM_SOURCENAME", length = 64)
    private String labItemSourceName;

    @Column(name = "LABITEM_IGNORE", columnDefinition = "SMALLINT(5)")
    private Short labItemIgnore;

    @Column(name = "ORDER_IDX")
    private Integer orderIdx;
}