package com.nikke.roster.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikke.roster.domain.entity.Unit;
import com.nikke.roster.domain.enums.*;
import com.nikke.roster.dto.RosterSyncResult;
import com.nikke.roster.ingestion.dto.RawRosterPayload;
import com.nikke.roster.ingestion.dto.RawRosterPayload.RawCharacterNode;
import com.nikke.roster.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RosterSyncService {

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

        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(is);
            List<RawCharacterNode> rawNodes;

            if (rootNode.isArray()) {
                rawNodes = objectMapper.convertValue(
                        rootNode,
                        new TypeReference<List<RawCharacterNode>>() {}
                );
            } else if (rootNode.has("NIKKECharacterData")) {
                rawNodes = objectMapper.convertValue(
                        rootNode.get("NIKKECharacterData"),
                        new TypeReference<List<RawCharacterNode>>() {}
                );
            } else {
                log.warn("Unrecognized JSON format in seed file {}", resourcePath);
                return new RosterSyncResult(0, 0, 0, "FAILED",
                        "Unrecognized JSON format in seed file");
            }

            if (rawNodes == null || rawNodes.isEmpty()) {
                log.warn("Seed file {} contains no characters", resourcePath);
                return new RosterSyncResult(0, 0, 0, "SUCCESS",
                        "No characters found to sync");
            }

            int insertCount = 0;
            int updatedCount = 0;
            List<Unit> unitsToSave = new ArrayList<>();

            for (RawCharacterNode node : rawNodes) {
                RawRosterPayload.RawCharacterData data = node.data() != null ? node.data() : null;
                if (data == null || data.name() == null) continue;

                String slug = (node.slug() != null && !node.slug().isBlank()) ? node.slug() : data.slug();
                if (slug == null || slug.isBlank()) continue;

                // Normalize Burst
                BurstStage burst = switch (String.valueOf(data.burstType()).trim().toUpperCase()) {
                    case "I", "BURST_I", "1" -> BurstStage.BURST_I;
                    case "II", "BURST_II", "2" -> BurstStage.BURST_II;
                    case "III", "BURST_III", "3" -> BurstStage.BURST_III;
                    default -> BurstStage.FLEXIBLE;
                };

                // Normalize Manufacturer
                Manufacturer mfg = switch (String.valueOf(data.manufacturer()).trim().toUpperCase()) {
                    case "ELYSION", "ELYSIUM" -> Manufacturer.ELYSION;
                    case "MISSILIS" -> Manufacturer.MISSILIS;
                    case "TETRA", "TETRA LINE" -> Manufacturer.TETRA;
                    case "PILGRIM" -> Manufacturer.PILGRIM;
                    case "ABNORMAL" -> Manufacturer.ABNORMAL;
                    default -> Manufacturer.ELYSION;
                };

                // Normalize Class
                ClassType classType = switch (String.valueOf(data.classType()).trim().toUpperCase()) {
                    case "ATTACKER" -> ClassType.ATTACKER;
                    case "DEFENDER" -> ClassType.DEFENDER;
                    case "SUPPORTER" -> ClassType.SUPPORTER;
                    default -> ClassType.ATTACKER;
                };

                // Normalize Element
                Element element = switch (String.valueOf(data.element()).trim().toUpperCase()) {
                    case "FIRE" -> Element.FIRE;
                    case "WATER" -> Element.WATER;
                    case "WIND" -> Element.WIND;
                    case "IRON" -> Element.IRON;
                    case "ELECTRIC" -> Element.ELECTRIC;
                    default -> Element.FIRE;
                };

                // Normalize Weapon
                WeaponType weapon = switch (String.valueOf(data.weapon()).trim().toUpperCase()) {
                    case "AR" -> WeaponType.AR;
                    case "SMG" -> WeaponType.SMG;
                    case "SG" -> WeaponType.SG;
                    case "SR" -> WeaponType.SR;
                    case "RL" -> WeaponType.RL;
                    case "MG" -> WeaponType.MG;
                    default -> WeaponType.AR;
                };

                // Normalize Rarity
                Rarity rarity;
                try {
                    rarity = Rarity.valueOf(String.valueOf(data.rarity()).trim().toUpperCase());
                } catch (Exception e) {
                    rarity = Rarity.SSR;
                }

                // Idempotent lookup by slug or unitCode
                Unit unit = unitRepository.findBySlug(slug)
                        .or(() -> unitRepository.findByUnitCode(slug))
                        .orElse(null);

                if (unit == null) {
                    unit = new Unit();
                    insertCount++;
                } else {
                    updatedCount++;
                }

                unit.setSlug(slug);
                unit.setUnitCode(slug);
                unit.setName(data.name().trim());
                unit.setRarity(rarity);
                unit.setManufacturer(mfg);
                unit.setClassType(classType);
                unit.setElement(element);
                unit.setWeaponType(weapon);
                unit.setBurstStage(burst);
                unit.setBackstory(data.backstory());
                unit.setCardImageUrl(data.cardImage());
                unit.setFullImageUrl(data.fullImage());
                unit.setImageUrl(data.fullImage() != null ? data.fullImage() : data.cardImage());

                unitsToSave.add(unit);
            }

            unitRepository.saveAll(unitsToSave);
            log.info("Roster sync completed. Processed: {}, Inserted: {}, Updated: {}",
                    unitsToSave.size(), insertCount, updatedCount);

            return new RosterSyncResult(unitsToSave.size(), insertCount, updatedCount, "SUCCESS",
                    "Sync completed successfully");
        } catch (Exception e) {
            log.error("Failed to sync roster catalog: {}", e.getMessage(), e);
            return new RosterSyncResult(0, 0, 0, "FAILED", e.getMessage());
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
