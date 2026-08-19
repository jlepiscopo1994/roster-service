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
public class BaseStats {

    @Column(name = "base_hp", nullable = false)
    private Integer hp;

    @Column(name = "base_atk", nullable = false)
    private Integer atk;

    @Column(name = "base_def", nullable = false)
    private Integer def;
}
