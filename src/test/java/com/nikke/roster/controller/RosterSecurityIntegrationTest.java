package com.nikke.roster.controller;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.Element;
import com.nikke.roster.domain.enums.Manufacturer;
import com.nikke.roster.domain.enums.Rarity;
import com.nikke.roster.domain.enums.WeaponType;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RosterSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RosterSyncService rosterSyncService;

    @MockBean
    private NikkeUnitRepository unitRepository;

    @BeforeEach
    void setUp() {
        RosterSyncResult defaultResult = RosterSyncResult.builder()
                .status("SUCCESS")
                .totalProcessed(2)
                .insertedCount(2)
                .updatedCount(0)
                .message("Sync successful")
                .build();
        when(rosterSyncService.syncFromClasspath()).thenReturn(defaultResult);
    }

    @Nested
    @DisplayName("Admin Sync Endpoint Security: POST /api/v1/admin/roster/sync")
    class AdminSyncEndpointSecurityTests {

        @Test
        @DisplayName("Should return 401 Unauthorized when request lacks a JWT token")
        void sync_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/admin/roster/sync"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as standard ROLE_COMMANDER")
        void sync_commanderRole_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/admin/roster/sync")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMMANDER"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 Forbidden when authenticated as CENTRAL_GOVERNMENT")
        void sync_centralGovernment_returns200() throws Exception {
            RosterSyncResult mockResult = RosterSyncResult.builder()
                    .status("SUCCESS")
                    .totalProcessed(1)
                    .insertedCount(1)
                    .updatedCount(0)
                    .message("Sync successful")
                    .build();

            when(rosterSyncService.syncFromClasspath()).thenReturn(mockResult);

            mockMvc.perform(post("/api/v1/admin/roster/sync")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CENTRAL_GOVERNMENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"));

        }
    }

    @Nested
    @DisplayName("Roster Query Endpoint Security: GET /api/v1/roster/units")
    class RosterQueryEndpointSecurityTests {

        @Test
        @DisplayName("Should return 401 Unauthorized when accessing units catalog without JWT token")
        void getUnits_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/roster/units"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 200 OK when accessing units catalog as ROLE_COMMANDER")
        void getUnits_commanderRole_returns200() throws Exception {
            NikkeUnit sampleUnit = NikkeUnit.builder()
                    .unitCode("NIKKE_RAPI")
                    .name("Rapi")
                    .rarity(Rarity.SR)
                    .element(Element.FIRE)
                    .weaponType(WeaponType.AR)
                    .manufacturer(Manufacturer.ELYSION)
                    .build();

            when(unitRepository.findAll()).thenReturn(List.of(sampleUnit));

            mockMvc.perform(get("/api/v1/roster/units")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMMANDER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Rapi"));
        }

        @Test
        @DisplayName("Should return 200 OK when accessing unit by code as ROLE_CENTRAL_GOVERNMENT")
        void getUnitCode_centralGovernmentRole_returns200() throws Exception {
            NikkeUnit sampleUnit = NikkeUnit.builder()
                    .unitCode("NIKKE_RAPI")
                    .name("Rapi")
                    .rarity(Rarity.SR)
                    .element(Element.FIRE)
                    .weaponType(WeaponType.AR)
                    .manufacturer(Manufacturer.ELYSION)
                    .build();

            when(unitRepository.findByUnitCode("NIKKE_RAPI")).thenReturn(Optional.of(sampleUnit));

            mockMvc.perform(get("/api/v1/roster/units/NIKKE_RAPI")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CENTRAL_GOVERNMENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Rapi"));
        }


    }
}
