package com.nikke.roster.controller;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class RosterControllersTest {

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

    @Test
    @DisplayName("POST /api/v1/admin/roster/sync should trigger sync and return 200 OK")
    void shouldTriggerRosterSync() throws Exception {
        RosterSyncResult result = RosterSyncResult.builder()
                .status("SUCCESS")
                .totalProcessed(2)
                .insertedCount(2)
                .updatedCount(0)
                .message("Sync successful")
                .build();

        when(rosterSyncService.syncFromClasspath()).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/roster/sync")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CENTRAL_GOVERNMENT")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.insertedCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/roster/units should return list of units")
    void shouldReturnAllUnits() throws Exception {
        NikkeUnit rapi = NikkeUnit.builder()
                .unitCode("NIKKE_RAPI")
                .name("Rapi")
                .rarity(Rarity.SR)
                .manufacturer(Manufacturer.ELYSION)
                .classType(ClassType.ATTACKER)
                .element(Element.FIRE)
                .weaponType(WeaponType.AR)
                .build();

        given(unitRepository.findAll()).willReturn(List.of(rapi));

        mockMvc.perform(get("/api/v1/roster/units")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMMANDER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].unitCode").value("NIKKE_RAPI"))
                .andExpect(jsonPath("$[0].name").value("Rapi"));
    }

    @Test
    @DisplayName("GET /api/v1/roster/units/{unitCode} should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        given(unitRepository.findByUnitCode("UNKNOWN")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/roster/units/UNKNOWN")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_COMMANDER"))))
                .andExpect(status().isNotFound());
    }
}
