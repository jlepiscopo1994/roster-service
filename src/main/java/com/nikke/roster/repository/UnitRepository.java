package com.nikke.roster.repository;

import com.nikke.roster.domain.entity.Unit;
import com.nikke.roster.domain.enums.Element;
import com.nikke.roster.domain.enums.Manufacturer;
import com.nikke.roster.domain.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    // Datamine Ingestion Query Methods
    Optional<Unit> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Existing Milestone 1 & 2 Query Methods
    Optional<Unit> findByUnitCode(String unitCode);
    List<Unit> findByManufacturer(Manufacturer manufacturer);
    List<Unit> findByElement(Element element);
    List<Unit> findByRarity(Rarity rarity);
}