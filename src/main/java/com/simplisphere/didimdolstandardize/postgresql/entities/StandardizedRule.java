package com.simplisphere.didimdolstandardize.postgresql.entities;

import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class StandardizedRule {
    @Id
    @GeneratedValue(generator = "standardized_rule_id_seq")
    @SequenceGenerator(name = "standardized_rule_id_seq", sequenceName = "standardized_rule_id_seq", allocationSize = 1, initialValue = 10000)
    private Long id;
    @Enumerated(EnumType.STRING)
    private RuleType type;
    private String name;
    private String description;
    private String fromName;
    private String toName;
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
}
