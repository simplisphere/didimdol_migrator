package com.simplisphere.didimdolstandardize.firebird;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CLIENT")
public class SosulClient {
    @Id
    @Column(name = "CLIENT_ID")
    Integer id;
    @Column(name = "CLIENT_NAME")
    private String name;
    @Column(name = "FAMILY_ID")
    private Integer familyId;
    @Column(name = "CLIENT_ADDRESS1")
    private String address;
}
