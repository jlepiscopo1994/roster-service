package com.nikke.roster.config;

import com.nikke.roster.repository.NikkeUnitRepository;
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

    private final NikkeUnitRepository unitRepository;
    private final RosterSyncService rosterSyncService;

    @Override
    public void run(String... args) {
        long currentUnitCount = unitRepository.count();

        if (currentUnitCount == 0) {
            log.info("Roster database catalog is empty. Initializing baseline startup seeding...");
            var result = rosterSyncService.syncFromClasspath();
            log.info("Startup seeding completed: {} (Inserted: {}, Updated: {})",
                    result.getStatus(), result.getInsertedCount(), result.getUpdatedCount());
        } else {
            log.info("Roster database already contains {} units. Skipping automatic seeding.", currentUnitCount);
        }
    }
}
