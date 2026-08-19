package com.nikke.roster.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class RosterSyncResult {
    private int totalProcessed;
    private int insertedCount;
    private int updatedCount;
    private String status;
    private String message;
}
