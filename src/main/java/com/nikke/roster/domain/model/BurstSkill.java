package com.nikke.roster.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nikke.roster.domain.enums.BurstType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BurstSkill {

    @JsonProperty("name")
    @Column(name = "burst_name", nullable = false)
    private String burstName;

    @JsonProperty("burstType")
    @Enumerated(EnumType.STRING)
    @Column(name = "burst_stage", nullable = false)
    private BurstType burstType;

    @JsonProperty("cooldownSeconds")
    @Column(name = "burst_cooldown_seconds", nullable = false)
    private Integer cooldownSeconds;

    @JsonProperty("description")
    @Column(name = "burst_description", columnDefinition = "TEXT", nullable = false)
    private String description;
}
