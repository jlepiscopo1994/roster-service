package com.nikke.roster.repository;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.BurstType;
import com.nikke.roster.domain.enums.Element;
import com.nikke.roster.domain.enums.Manufacturer;
import com.nikke.roster.domain.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NikkeUnitRepository extends JpaRepository<NikkeUnit, UUID> {

    Optional<NikkeUnit> findByUnitCode(String unitCode);

    boolean existsByUnitCode(String unitCode);

    List<NikkeUnit> findByManufacturer(Manufacturer manufacturer);

    List<NikkeUnit> findByElement(Element element);

    List<NikkeUnit> findByRarity(Rarity rarity);
}
