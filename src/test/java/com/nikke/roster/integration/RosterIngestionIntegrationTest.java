package com.nikke.roster.integration;

import com.nikke.roster.domain.entity.Unit;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.UnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RosterIngestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RosterSyncService rosterSyncService;

    @Autowired
    private UnitRepository unitRepository;

    @BeforeEach
    void setup() {
        unitRepository.deleteAll();
    }

    @Test
    @DisplayName("Should ingest seed JSON and verify database persistence")
    void shouldIngestAndPersistUnits() {
        RosterSyncResult result = rosterSyncService.syncFromClasspath();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getInsertedCount()).isGreaterThan(0);

        List<Unit> units = unitRepository.findAll();
        assertThat(units).isNotEmpty();
    }

    @Test
    @DisplayName("Should update existing records idempotently on second sync")
    void shouldBeIdempotent() {
        rosterSyncService.syncFromClasspath();
        long initialCount = unitRepository.count();

        RosterSyncResult secondResult = rosterSyncService.syncFromClasspath();
        long secondCount = unitRepository.count();

        assertThat(secondCount).isEqualTo(initialCount);
        assertThat(secondResult.getUpdatedCount()).isEqualTo((int) initialCount);
        assertThat(secondResult.getInsertedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should sync and query via REST endpoints with JWT authorization")
    void shouldSyncAndQueryViaRestEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/admin/roster/sync")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CENTRAL_GOVERNMENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/roster/units")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMMANDER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}