package com.nikke.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class RosterSyncResult {
    private int totalProcessed;
    private int insertedCount;
    private int updatedCount;
    private String status;
    private String message;
}
