package com.simplisphere.didimdolstandardize.mssql.entities;

import com.simplisphere.didimdolstandardize.mssql.dtos.query.ClientPetDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "pt")
@SqlResultSetMapping(
        name = "ClientPetMapping",
        classes = @ConstructorResult(
                targetClass = ClientPetDto.class,
                columns = {
                        @ColumnResult(name = "clientId", type = Long.class),
                        @ColumnResult(name = "clientName", type = String.class),
                        @ColumnResult(name = "clientAddress1", type = String.class),
                        @ColumnResult(name = "clientAddress2", type = String.class),
                        @ColumnResult(name = "petId", type = Long.class),
                        @ColumnResult(name = "petName", type = String.class),
                        @ColumnResult(name = "speciesName", type = String.class),
                        @ColumnResult(name = "breed", type = String.class),
                        @ColumnResult(name = "sex", type = String.class),
                        @ColumnResult(name = "birth", type = LocalDate.class),
                        @ColumnResult(name = "color", type = String.class),
                        @ColumnResult(name = "petFirstDate", type = LocalDate.class),
                        @ColumnResult(name = "petLastDate", type = LocalDate.class)
                }
        )
)
//@SqlResultSetMapping(
//        name = "ClientPetMapping",
//        classes = @ConstructorResult(
//                targetClass = ClientPet.class,
//                columns = {
//                        @ColumnResult(name = "clientId", type = Long.class),
//                        @ColumnResult(name = "clientName", type = String.class),
//                        @ColumnResult(name = "clientAddress1", type = String.class),
//                        @ColumnResult(name = "clientAddress2", type = String.class),
//                        @ColumnResult(name = "petId", type = Long.class),
//                        @ColumnResult(name = "petName", type = String.class),
//                        @ColumnResult(name = "speciesName", type = String.class),
//                        @ColumnResult(name = "breed", type = String.class),
//                        @ColumnResult(name = "sex", type = String.class),
//                        @ColumnResult(name = "birth", type = LocalDate.class),
//                        @ColumnResult(name = "color", type = String.class),
//                        @ColumnResult(name = "petFirstDate", type = LocalDate.class),
//                        @ColumnResult(name = "petLastDate", type = LocalDate.class)
//                }
//        )
//)
//@NamedNativeQuery(
//        name = "MsPet.findClientPetDetails",
//        query = "SELECT c.clid AS clientId, c.cllname AS clientName, c.claddr AS clientAddress1, c.claddr2 AS clientAddress2, " +
//                "p.ptid AS petId, p.ptname AS petName, s.spdesc AS speciesName, b.brcode AS breed, sx.sxdesc AS sex, " +
//                "p.ptdob AS birth, p.ptclr AS color, p.ptfdat AS petFirstDate, p.ptldat AS petLastDate " +
//                "FROM pt p " +
//                "JOIN cl c ON p.ptclid = c.clid " +
//                "JOIN sp s ON p.ptspid = s.spid " +
//                "JOIN br b ON p.ptbrid = b.brid " +
//                "JOIN sx ON p.ptsxid = sx.sxid",
//        resultSetMapping = "ClientPetMapping"
//)
public class MsPet {

    @Id
    @Column(name = "ptid")
    private Integer id;

    //    @Column(name = "ptclid", nullable = false)
//    private int clientId;
//
    @Column(name = "ptname", length = 40, columnDefinition = "CHAR(40)")
    private String name;

    //    @Column(name = "ptspid")
//    private Integer speciesId;
//
//    @Column(name = "ptbrid")
//    private Integer breedId;
//
//    @Column(name = "ptsxid")
//    private Integer sexId;
//
    @Column(name = "ptclr", length = 40, columnDefinition = "CHAR(40)")
    private String color;
    //
//    @Column(name = "ptwgt")
//    private Float weight;
//
    @Column(name = "ptdob")
    private LocalDateTime birth;

    @Column(name = "ptfdat")
    private LocalDateTime firstVisitDate;

    @Column(name = "ptldat")
    private LocalDateTime lastVisitDate;
//
//    @Column(name = "ptyptid")
//    private Integer typeId;
//
//    @Column(name = "ptcmt", length = 2048)
//    private String comments;
//
//    @Column(name = "ptactdat")
//    private LocalDateTime activeDate;
//
//    @Column(name = "ptemid")
//    private Integer employeeId;
//
//    @Column(name = "pttag", length = 20, columnDefinition = "CHAR(20)")
//    private String tag;
//
//    @Column(name = "ptchip", length = 20, columnDefinition = "CHAR(20)")
//    private String chip;
//
//    @Column(name = "ptlic", length = 20, columnDefinition = "CHAR(20)")
//    private String license;
//
//    @Column(name = "ptdcs")
//    private Byte dcs;
//
//    @Column(name = "ptdcsdat")
//    private LocalDateTime dcsDate;
//
//    @Column(name = "ptcustom0", length = 80, columnDefinition = "CHAR(80)")
//    private String custom0;
//
//    @Column(name = "ptcustom1", length = 80, columnDefinition = "CHAR(80)")
//    private String custom1;
//
//    @Column(name = "ptcustom2", length = 80, columnDefinition = "CHAR(80)")
//    private String custom2;
//
//    @Column(name = "ptcustom3", length = 80, columnDefinition = "CHAR(80)")
//    private String custom3;
//
//    @Column(name = "ptcustom4", length = 80, columnDefinition = "CHAR(80)")
//    private String custom4;
//
//    @Column(name = "ptcustom5", length = 80, columnDefinition = "CHAR(80)")
//    private String custom5;
//
//    @Column(name = "ptcustom6", length = 80, columnDefinition = "CHAR(80)")
//    private String custom6;
//
//    @Column(name = "ptcustom7", length = 80, columnDefinition = "CHAR(80)")
//    private String custom7;
//
//    @Column(name = "ptcustom8", length = 80, columnDefinition = "CHAR(80)")
//    private String custom8;
//
//    @Column(name = "ptcustom9", length = 80, columnDefinition = "CHAR(80)")
//    private String custom9;
//
//    @Column(name = "ptnew")
//    private Byte isNew;
//
//    @Column(name = "ptact", nullable = false)
//    private Byte isActive;
//
//    @Column(name = "ptalert")
//    private Byte alert;
//
//    @Column(name = "ptoldid", length = 40, columnDefinition = "CHAR(40)")
//    private String oldId;
//
//    @Column(name = "ptmisc", length = 2048)
//    private String misc;
//
//    @Column(name = "ptfcid", nullable = false)
//    private int fcId;
//
//    @Column(name = "ptwgtunit")
//    private Byte weightUnit;
//
//    @Column(name = "ptdcstype")
//    private Byte dcsType;
//
//    @Column(name = "ptnodob")
//    private Byte noDateOfBirth;
//
//    @Column(name = "ptnodcs")
//    private Byte noDcs;
//
//    @Column(name = "ptins")
//    private Byte insured;
//
//    @Column(name = "ptinsplid")
//    private Byte insurancePlanId;
//
//    @Column(name = "ptpfemid")
//    private Integer pfEmployeeId;
//
//    @Column(name = "ptfcid1")
//    private Integer fcId1;
//
//    @Column(name = "ptfcid2")
//    private Integer fcId2;
//
//    @Column(name = "ptmember")
//    private Byte member;
//
//    @Column(name = "ptremid")
//    private Integer remId;
//
//    @Column(name = "ptmemid")
//    private Integer memId;
//
//    @Column(name = "ptaddog")
//    private Byte additionalDog;
//
//    @Column(name = "ptadtag", length = 100, columnDefinition = "CHAR(100)")
//    private String adTag;
//
//    @Column(name = "pttrndt")
//    private LocalDateTime transferDate;
//
//    @Column(name = "ptfcchk")
//    private Byte fcCheck;
//
//    @Column(name = "ptfcchk1")
//    private Byte fcCheck1;
//
//    @Column(name = "ptfcchk2")
//    private Byte fcCheck2;
//
//    @Column(name = "ptmemno", length = 30, columnDefinition = "CHAR(30)")
//    private String memberNumber;
//
//    @Column(name = "ptstallnum", length = 50, columnDefinition = "CHAR(50)")
//    private String stallNumber;
//
//    @Column(name = "ptgrprx")
//    private Byte groupRx;
//
//    @Column(name = "pterxspcode", length = 14, columnDefinition = "CHAR(14)")
//    private String erxSpCode;
//
//    @Column(name = "pterxbrcode", length = 6, columnDefinition = "CHAR(6)")
//    private String erxBrCode;
//
//    @Column(name = "ptgrprxcnt")
//    private Integer groupRxCount;
//
//    @Column(name = "ptmanage")
//    private Byte manage;
//
//    @Column(name = "ptecgid", length = 50, columnDefinition = "CHAR(50)")
//    private String ecgId;
//
//    @Column(name = "ptfcid3")
//    private Integer fcId3;
//
//    @Column(name = "ptfcchk3")
//    private Byte fcCheck3;
//
//    @Column(name = "ptdiabetes")
//    private Byte diabetes;
//
//    @Column(name = "ptmngcmt", length = 200)
//    private String manageComments;
//
//    @Column(name = "ptblood", length = 6, columnDefinition = "CHAR(6)")
//    private String blood;
//
//    @Column(name = "ptinsnum", length = 50, columnDefinition = "CHAR(50)")
//    private String insuranceNumber;
//
//    @Column(name = "ptinsseq", length = 10, columnDefinition = "CHAR(10)")
//    private String insuranceSequence;
//
//    @Column(name = "ptciwgt")
//    private String currentWeight;
//
//    @Column(name = "ptcidob")
//    private String currentDob;
}