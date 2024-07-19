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
@Table(name = "TRXDATA")
@Comment("처방 데이터")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "SosulTrxData.chartAndPet",
                attributeNodes = {
                        @NamedAttributeNode(value = "chart", subgraph = "chart-with-pet")
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "chart-with-pet",
                                attributeNodes = {
                                        @NamedAttributeNode("sosulPet")
                                }
                        )
                }
        )
})
public class SosulTrxData {
    @Id
    @Column(name = "TRXDATA_ID", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "CHART_ID", nullable = false)
    private SosulChart chart;

    @Column(name = "TRXDATA_NAME", length = 200)
    private String name;

    @Column(name = "TRXDATA_UNIT", length = 16)
    private String unit;

    @Column(name = "TRXDATA_DAY", columnDefinition = "INTEGER default -1")
    private Integer perDay;

    @Column(name = "TRXDATA_TOTAL", columnDefinition = "INTEGER default -1")
    private Integer total;

    @Column(name = "TRXDATA_ROUTE")
    private String route;

    @Column(name = "SIGN_NAME")
    private String sign;

    @Column(name = "TRXDATA_INDEX")
    private Integer index;

//    @Column(name = "CREATED")
//    private LocalDateTime created;

//    @Column(name = "MODIFIED")
//    private LocalDateTime modified;
}
