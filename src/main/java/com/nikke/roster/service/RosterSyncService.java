package com.nikke.roster.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikke.roster.domain.entity.Unit;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RosterSyncService {

    private static final Logger log = LoggerFactory.getLogger(RosterSyncService.class);
    private static final String DEFAULT_SEED_FILE = "data/nikke-roster-seed.json";

    private final UnitRepository unitRepository;
    private final ObjectMapper objectMapper;

    /**
     * Synchronizes the database catalog with the default seed JSON file.
     */
    @Transactional
    public RosterSyncResult syncFromClasspath() {
        return syncFromClasspath(DEFAULT_SEED_FILE);
    }

    /**
     * Synchronizes the database catalog with a specific classpath seed file
     */
    @Transactional
    public RosterSyncResult syncFromClasspath(String resourcePath) {
        log.info("Starting roster synchronization from resource: {}", resourcePath);

        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IllegalStateException("Seed file not found at path: " + resourcePath);
            }

            // Ensure lenient deserialization
            ObjectMapper mapper = objectMapper.copy()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            try (InputStream inputStream = resource.getInputStream()) {
                List<Unit> incomingUnits = mapper.readValue(
                        inputStream,
                        new TypeReference<>() {
                        }
                );

                return processBatchUpsert(incomingUnits);
            }
        } catch (Exception e) {
            log.error("Failed to sync roster catalog: {}", e.getMessage(), e);
            return RosterSyncResult.builder()
                    .totalProcessed(0)
                    .insertedCount(0)
                    .updatedCount(0)
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Idempotent batch upsert logic: inserts new records or updates existing ones based on unitCode.
     */
    @Transactional
    public RosterSyncResult processBatchUpsert(List<Unit> incomingUnits) {
        int inserted = 0;
        int updated = 0;

        for (Unit incoming : incomingUnits) {
            Optional<Unit> existingOpt = unitRepository.findByUnitCode(incoming.getUnitCode());

            if (existingOpt.isPresent()) {
                Unit existing = existingOpt.get();
                updateExistingUnit(existing, incoming);
                unitRepository.save(existing);
                updated++;
                log.debug("Updated unit: {}", incoming.getUnitCode());
            } else {
                unitRepository.save(incoming);
                inserted++;
                log.debug("Inserted new unit: {}", incoming.getUnitCode());
            }
        }

        log.info("Roster sync completed. Processed: {}, Inserted: {}, Updated: {}",
                incomingUnits.size(), inserted, updated);

        return RosterSyncResult.builder()
                .totalProcessed(incomingUnits.size())
                .insertedCount(inserted)
                .updatedCount(updated)
                .status("SUCCESS")
                .message("Roster catalog successfully synchronized.")
                .build();
    }

    private void updateExistingUnit(Unit target, Unit source) {
        target.setName(source.getName());
        target.setRarity(source.getRarity());
        target.setManufacturer(source.getManufacturer());
        target.setClassType(source.getClassType());
        target.setElement(source.getElement());
        target.setWeaponType(source.getWeaponType());
        target.setBaseStats(source.getBaseStats());
        target.setNormalAttack(source.getNormalAttack());
        target.setSkill1(source.getSkill1());
        target.setSkill2(source.getSkill2());
        target.setBurstSkill(source.getBurstSkill());
        target.setImageUrl(source.getImageUrl());
    }

}
