package com.nikke.roster.integration;

import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.UnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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

    @Test
    @DisplayName("Should ingest full roster and verify catalog count >= 100")
    void shouldIngestAndPersistUnits() {
        RosterSyncResult result = rosterSyncService.syncFromClasspath();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalProcessed()).isGreaterThanOrEqualTo(100);
        assertThat(unitRepository.count()).isGreaterThanOrEqualTo(100);
    }

    @Test
    @DisplayName("Should remain idempotent when syncing multiple times")
    void shouldBeIdempotent() {
        RosterSyncResult firstRun = rosterSyncService.syncFromClasspath();
        long countAfterFirst = unitRepository.count();

        RosterSyncResult secondRun = rosterSyncService.syncFromClasspath();
        long countAfterSecond = unitRepository.count();

        assertThat(firstRun.getStatus()).isEqualTo("SUCCESS");
        assertThat(secondRun.getStatus()).isEqualTo("SUCCESS");
        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
        assertThat(secondRun.getInsertedCount()).isEqualTo(0);
        assertThat(secondRun.getUpdatedCount()).isEqualTo(firstRun.getTotalProcessed());
    }

    @Test
    @DisplayName("Should sync via Admin REST endpoint and query roster units with Commander role")
    @WithMockUser(username = "central_admin", roles = {"CENTRAL_GOVERNMENT"})
    void shouldSyncAndQueryViaRestEndpoints() throws Exception {
        // Trigger admin sync
        mockMvc.perform(post("/api/v1/admin/roster/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalProcessed", greaterThanOrEqualTo(100)));

        // Query public roster with commander role
        mockMvc.perform(get("/api/v1/roster/units")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("commander").roles("COMMANDER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(100))));
    }
}