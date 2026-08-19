package com.nikke.roster.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.domain.model.BaseStats;
import com.nikke.roster.domain.model.BurstSkill;
import com.nikke.roster.domain.model.Skill;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RosterSyncServiceTest {

    @Mock
    private NikkeUnitRepository unitRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RosterSyncService rosterSyncService;

    private NikkeUnit sampleUnit;

    @BeforeEach
    void setUp() {
        sampleUnit = NikkeUnit.builder()
                .id(UUID.randomUUID())
                .unitCode("NIKKE_RAPI")
                .name("Rapi")
                .rarity(Rarity.SR)
                .manufacturer(Manufacturer.ELYSION)
                .classType(ClassType.ATTACKER)
                .element(Element.FIRE)
                .weaponType(WeaponType.AR)
                .baseStats(BaseStats.builder().hp(583420).atk(25410).def(3920).build())
                .normalAttacks(Skill.builder().name("Rifle").type("Active").cooldownSeconds(0).description("DMG").build())
                .skill1(Skill.builder().name("S1").type("Passive").cooldownSeconds(0).description("Buff").build())
                .skill2(Skill.builder().name("S2").type("Active").cooldownSeconds(20).description("Nuke").build())
                .burstSkill(BurstSkill.builder().burstName("Burst").burstType(BurstType.BURST_III).cooldownSeconds(40).description("Big Nuke").build())
                .imageUrl("https://example.com/rapi.png")
                .build();
    }

    @Test
    @DisplayName("Should insert units when they do not exist in the database")
    void shouldInsertUnitsWhenNotExists() {
        when(unitRepository.findByUnitCode("NIKKE_RAPI")).thenReturn(Optional.empty());
        when(unitRepository.save(any(NikkeUnit.class))).thenReturn(sampleUnit);

        RosterSyncResult result = rosterSyncService.processBatchUpsert(List.of(sampleUnit));

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getInsertedCount()).isEqualTo(1);
        assertThat(result.getUpdatedCount()).isEqualTo(0);

        verify(unitRepository, times(1)).save(sampleUnit);
    }

    @Test
    @DisplayName("Should update units when they already exist in the database (Idempotent)")
    void shouldUpdateUnitsWhenExists() {
        NikkeUnit existingUnit = NikkeUnit.builder()
                .id(UUID.randomUUID())
                .unitCode("NIKKE_RAPI")
                .name("Old Rapi")
                .rarity(Rarity.SR)
                .manufacturer(Manufacturer.ELYSION)
                .classType(ClassType.ATTACKER)
                .element(Element.FIRE)
                .weaponType(WeaponType.AR)
                .build();

        when(unitRepository.findByUnitCode("NIKKE_RAPI")).thenReturn(Optional.of(existingUnit));
        when(unitRepository.save(any(NikkeUnit.class))).thenReturn(existingUnit);

        RosterSyncResult result = rosterSyncService.processBatchUpsert(List.of(sampleUnit));

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getInsertedCount()).isEqualTo(0);
        assertThat(result.getUpdatedCount()).isEqualTo(1);
        assertThat(existingUnit.getName()).isEqualTo("Rapi");

        verify(unitRepository, times(1)).save(existingUnit);
    }

    @Test
    @DisplayName("Should load and parse nikke-roster-seed.json from classpath")
    void shouldSyncFromClasspath() {
        when(unitRepository.findByUnitCode(anyString())).thenReturn(Optional.empty());
        when(unitRepository.save(any(NikkeUnit.class))).thenAnswer(i -> i.getArgument(0));

        RosterSyncResult result = rosterSyncService.syncFromClasspath();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalProcessed()).isEqualTo(2); // Rapi and Anis from our seed file
        assertThat(result.getInsertedCount()).isEqualTo(2);
        assertThat(result.getUpdatedCount()).isEqualTo(0);
    }
}