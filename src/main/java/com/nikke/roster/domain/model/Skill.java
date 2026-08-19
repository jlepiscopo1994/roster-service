package com.nikke.roster.domain.model;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g. "Active" or "Passive"

    @Column(name = "cooldown_seconds", nullable = false)
    private Integer cooldownSeconds;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
}
