package com.nikke.roster.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("type")
    @Column(nullable = false)
    private String type; // e.g. "Active" or "Passive"

    @JsonProperty("cooldownSeconds")
    @Column(name = "cooldown_seconds", nullable = false)
    private Integer cooldownSeconds;

    @JsonProperty("description")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
}
