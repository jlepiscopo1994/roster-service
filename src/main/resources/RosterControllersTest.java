package com.nikke.roster.controller;

import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.NikkeUnitRepository;
import com.nikke.roster.service.RosterSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;

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

        given(rosterSyncService.)
    }
}
