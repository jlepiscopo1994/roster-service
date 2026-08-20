package com.nikke.roster.repository;

import com.nikke.roster.domain.entity.Unit;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.domain.model.BaseStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UnitRepositoryTest {

    @Autowired
    private UnitRepository unitRepository;

    @Test
    @DisplayName("Should persist unit and query by unique slug")
    void shouldPersistAndFindBySlug() {
        Unit unit = Unit.builder()
                .slug("modernia")
                .unitCode("NIKKE_MODERNIA")
                .name("Modernia")
                .rarity(Rarity.SSR)
                .manufacturer(Manufacturer.PILGRIM)
                .classType(ClassType.ATTACKER)
                .element(Element.FIRE)
                .weaponType(WeaponType.MG)
                .burstStage(BurstStage.BURST_III)
                .baseStats(BaseStats.builder().atk(25000).def(4000).hp(580000).build())
                .backstory("Her mind wiped clean, she fights alongside the Commander.")
                .cardImageUrl("https://cdn.example.com/modernia_card.png")
                .fullImageUrl("https://cdn.example.com/modernia_full.png")
                .build();

        unitRepository.save(unit);

        Optional<Unit> found = unitRepository.findBySlug("modernia");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Modernia");
        assertThat(found.get().getManufacturer()).isEqualTo(Manufacturer.PILGRIM);
        assertThat(unitRepository.existsBySlug("modernia")).isTrue();
        assertThat(unitRepository.existsBySlug("unknown-slug")).isFalse();
    }
}