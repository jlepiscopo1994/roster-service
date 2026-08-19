package com.nikke.roster.controller;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(controllers = {RosterAdminController.class, RosterQueryController.class})
public class RosterControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RosterSyncService rosterSyncService;

    @MockBean
    private NikkeUnitRepository unitRepository;

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

        given(rosterSyncService.syncFromClasspath()).willReturn(result);

        mockMvc.perform(post("/api/v1/admin/roster/sync")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalProcessed").value(2));
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

        mockMvc.perform(get("/api/v1/roster/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].unitCode").value("NIKKE_RAPI"))
                .andExpect(jsonPath("$[0].name").value("Rapi"));
    }

    @Test
    @DisplayName("GET /api/v1/roster/units/{unitCode} should return 404 when not found")
    void shouldReturn404WhenNotFound() throws Exception {
        given(unitRepository.findByUnitCode("UNKNOWN")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/roster/units/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}
