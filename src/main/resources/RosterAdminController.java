package com.nikke.roster.controller;

import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.service.RosterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/roster")
@RequiredArgsConstructor
public class RosterAdminController {

    private final RosterSyncService rosterSyncService;

    /**
     * Administrative trigger for syncing the roster database from the seed dataset.
     * In Milestone 2, this endpoint will be secured with ROLE_CENTRAL_GOVERNMENT.
     */
    @PostMapping("/sync")
    public ResponseEntity<RosterSyncResult> triggerRosterSync() {
        RosterSyncResult result = rosterSyncService.syncFromClasspath();
        if ("SUCCESS".equalsIgnoreCase(result.getStatus())) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.internalServerError().body(result);
    }
}
