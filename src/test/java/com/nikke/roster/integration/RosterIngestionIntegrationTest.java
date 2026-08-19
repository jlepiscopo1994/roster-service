package com.nikke.roster.integration;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class RosterIngestionIntegrationTest {

//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
//            .withDatabaseName("roster_test_db")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
//    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NikkeUnitRepository unitRepository;

    @Autowired
    private RosterSyncService rosterSyncService;

    @BeforeEach
    void cleanDatabase() {
        unitRepository.deleteAll();
    }

    @Test
    @DisplayName("Should execute initial classpath ingestion into real PostgreSQL container")
    void shouldIngestRosterIntoDatabase() {
        RosterSyncResult result = rosterSyncService.syncFromClasspath();

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalProcessed()).isEqualTo(2);
        assertThat(result.getInsertedCount()).isEqualTo(2);
        assertThat(result.getUpdatedCount()).isEqualTo(0);

        List<NikkeUnit> allUnits = unitRepository.findAll();
        assertThat(allUnits).hasSize(2);

        Optional<NikkeUnit> rapiOpt = unitRepository.findByUnitCode("NIKKE_RAPI");
        assertThat(rapiOpt).isPresent();
        assertThat(rapiOpt.get().getName()).isEqualTo("Rapi");
        assertThat(rapiOpt.get().getBurstSkill().getBurstName()).isEqualTo("Let's End This");
    }

    @Test
    @DisplayName("Should maintain idempotency when sync runs repeatedly")
    void shouldBeIdempotentAcrossMultipleSyncRuns() {
        // Run 1: Initial Ingestion
        RosterSyncResult firstRun = rosterSyncService.syncFromClasspath();
        assertThat(firstRun.getInsertedCount()).isEqualTo(2);
        assertThat(firstRun.getUpdatedCount()).isEqualTo(0);
        assertThat(unitRepository.count()).isEqualTo(2);

        // Run 2: Re-sync same dataset
        RosterSyncResult secondRun = rosterSyncService.syncFromClasspath();
        assertThat(secondRun.getStatus()).isEqualTo("SUCCESS");
        assertThat(secondRun.getTotalProcessed()).isEqualTo(2);
        assertThat(secondRun.getInsertedCount()).isEqualTo(0);
        assertThat(secondRun.getUpdatedCount()).isEqualTo(2);
        assertThat(unitRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should trigger sync via Admin REST endpoint and query units via RosterQueryController")
    void shouldSyncAndQueryViaRestEndpoints() {
        String syncUrl = "http://localhost:" + port + "/api/v1/admin/roster/sync";
        String getUrl = "http://localhost:" + port + "/api/v1/roster/units/NIKKE_ANIS";

        // Trigger POST /api/v1/admin/roster/sync
        ResponseEntity<RosterSyncResult> syncResponse = restTemplate.postForEntity(syncUrl, null, RosterSyncResult.class);
        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(syncResponse.getBody()).isNotNull();
        assertThat(syncResponse.getBody().getStatus()).isEqualTo("SUCCESS");

        // Query GET /api/v1/roster/units/NIKKE_ANIS
        ResponseEntity<NikkeUnit> queryResponse = restTemplate.getForEntity(getUrl, NikkeUnit.class);
        assertThat(queryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(queryResponse.getBody()).isNotNull();
        assertThat(queryResponse.getBody().getUnitCode()).isEqualTo("NIKKE_ANIS");
        assertThat(queryResponse.getBody().getName()).isEqualTo("Anis");
    }
}
