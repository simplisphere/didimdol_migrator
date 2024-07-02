package com.simplisphere.didimdolstandardize.firebird.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "LABPRODUCT")
@Comment("검사 제품 정보")
public class SosulLabProduct {

    @Id
    @Column(name = "LABPRODUCT_ID", nullable = false)
    private Integer labProductId;

    @Column(name = "LABPRODUCT_NAME", length = 30)
    private String labProductName;

    @Column(name = "LABPRODUCT_COMPANY", length = 40)
    private String labProductCompany;

    @Column(name = "LABPRODUCT_TYPE", columnDefinition = "SMALLINT(5) default 0")
    private Short labProductType = 0;

    @Column(name = "LABPRODUCT_USE", columnDefinition = "SMALLINT(5) default 0")
    private Short labProductUse = 0;

    @Column(name = "ORDER_IDX")
    private Integer orderIdx;
}