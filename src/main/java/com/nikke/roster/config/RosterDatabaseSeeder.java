package com.nikke.roster.config;

import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.UnitRepository;
import com.nikke.roster.service.RosterSyncService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RosterDatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RosterDatabaseSeeder.class);

    private final UnitRepository unitRepository;
    private final RosterSyncService rosterSyncService;

    @Override
    public void run(String... args) {
        if (unitRepository.count() == 0) {
            log.info("Roster database catalog is empty. Initiating baseline startup seeding...");
            try {
                RosterSyncResult results = rosterSyncService.syncFromClasspath();
                if (results != null && "SUCCESS".equalsIgnoreCase(results.getStatus())) {
                    log.info("Startup seeding completed: SUCCESS (Inserted: {}, Updated: {})",
                            results.getInsertedCount(), results.getUpdatedCount());
                } else if (results != null) {
                    log.warn("Startup seeding completed with status: {}", results.getStatus());
                }
            } catch (Exception e) {
                log.warn("Startup seeding skipped or encountered exception: {}", e.getMessage());
            }
        }
    }

}
