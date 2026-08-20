package com.nikke.roster.domain.entity;

import com.nikke.roster.domain.enums.*;
import com.nikke.roster.domain.model.BaseStats;
import com.nikke.roster.domain.model.BurstSkill;
import com.nikke.roster.domain.model.Skill;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "unit_code")
    private String unitCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Manufacturer manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false)
    private ClassType classType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Element element;

    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", nullable = false)
    private WeaponType weaponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "burst_stage", nullable = false)
    private BurstStage burstStage;

    @Column(length = 4000)
    private String backstory;

    @Column(name = "card_image_url")
    private String cardImageUrl;

    @Column(name = "full_image_url")
    private String fullImageUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Embedded
    private BaseStats baseStats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "normal_attack_name")),
            @AttributeOverride(name = "type", column = @Column(name = "normal_attack_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "normal_attack_cooldown_seconds")),
            @AttributeOverride(name = "description", column = @Column(name = "normal_attack_desc", length = 2000))
    })
    private Skill normalAttack;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "skill1_name")),
            @AttributeOverride(name = "type", column = @Column(name = "skill1_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "skill1_cooldown_seconds")),
            @AttributeOverride(name = "description", column = @Column(name = "skill1_desc", length = 2000))
    })
    private Skill skill1;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "skill2_name")),
            @AttributeOverride(name = "type", column = @Column(name = "skill2_type")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "skill2_cooldown_seconds")),
            @AttributeOverride(name = "description", column = @Column(name = "skill2_desc", length = 2000))
    })
    private Skill skill2;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "burstName", column = @Column(name = "burst_skill_name")),
            @AttributeOverride(name = "burstStage", column = @Column(name = "burst_skill_stage")),
            @AttributeOverride(name = "cooldownSeconds", column = @Column(name = "burst_skill_cooldown_seconds")),
            @AttributeOverride(name = "description", column = @Column(name = "burst_skill_desc", length = 2000))
    })
    private BurstSkill burstSkill;

    public String getUnitCode() {
        return unitCode != null ? unitCode : slug;
    }
}