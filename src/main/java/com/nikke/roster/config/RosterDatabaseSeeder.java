package com.nikke.roster.config;

import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.UnitRepository;
import com.nikke.roster.service.RosterSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RosterDatabaseSeeder implements CommandLineRunner {

    private final UnitRepository unitRepository;
    private final RosterSyncService rosterSyncService;

    @Override
    public void run(String... args) {
        long currentCount = unitRepository.count();

        if (currentCount == 0) {
            log.info("Roster database catalog is empty. Initiating baseline startup seeding from datamine...");
            RosterSyncResult result = rosterSyncService.syncFromClasspath();
            if (result != null) {
                log.info("Startup seeding completed with status: {}. Total units seeded: {}",
                        result.getStatus(), result.getInsertedCount());
            } else {
                log.info("Startup seeding completed (null result from mocked service).");
            }
        } else {
            log.info("Roster database already populated with {} Nikkes. Skipping startup seed.", currentCount);
        }
    }
}