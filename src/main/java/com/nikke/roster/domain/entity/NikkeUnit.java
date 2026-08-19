package com.nikke.roster.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.domain.model.BaseStats;
import com.nikke.roster.domain.model.BurstSkill;
import com.nikke.roster.domain.model.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "nikke_units",
        indexes = {
                @Index(name = "idx_unit_code", columnList = "unitCode", unique = true),
                @Index(name = "idx_manufacturer", columnList = "manufacturer"),
                @Index(name = "idx_element", columnList = "element")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NikkeUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("unitCode")
    @Column(nullable = false, unique = true, length = 64)
    private String unitCode;

    @JsonProperty("name")
    @Column(nullable = false, length = 128)
    private String name;

    @JsonProperty("rarity")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Rarity rarity;

    @JsonProperty("manufacturer")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Manufacturer manufacturer;

    @JsonProperty("classType")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClassType classType;

    @JsonProperty("element")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Element element;

    @JsonProperty("weaponType")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WeaponType weaponType;

    @JsonProperty("baseStats")
    @Embedded
    private BaseStats baseStats;

    @JsonProperty("normalAttack")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "norm_attack_name")),
            @AttributeOverride(name = "type", column = @Column(name = "norm_attack_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "norm_attack_cd")),
            @AttributeOverride(name = "description", column = @Column(name = "norm_attack_desc", columnDefinition = "TEXT"))
    })
    private Skill normalAttack;

    @JsonProperty("skill1")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "skill1_name")),
            @AttributeOverride(name = "type", column = @Column(name = "skill1_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "skill1_cd")),
            @AttributeOverride(name = "description", column = @Column(name = "skill1_desc", columnDefinition = "TEXT"))
    })
    private Skill skill1;

    @JsonProperty("skill2")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "skill2_name")),
            @AttributeOverride(name = "type", column = @Column(name = "skill2_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "skill2_cd")),
            @AttributeOverride(name = "description", column = @Column(name = "skill2_desc", columnDefinition = "TEXT"))
    })
    private Skill skill2;

    @JsonProperty("burstSkill")
    @Embedded
    private BurstSkill burstSkill;

    @JsonProperty("imageUrl")
    @Column(name = "image_url", length = 512)
    private String imageUrl;

}
