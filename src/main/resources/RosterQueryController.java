package com.nikke.roster.controller;

import com.nikke.roster.domain.entity.NikkeUnit;
import com.nikke.roster.domain.enums.Element;
import com.nikke.roster.domain.enums.Manufacturer;
import com.nikke.roster.domain.enums.Rarity;
import com.nikke.roster.repository.NikkeUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roster/units")
@RequiredArgsConstructor
public class RosterQueryController {

    private final NikkeUnitRepository unitRepository;

    @GetMapping
    public ResponseEntity<List<NikkeUnit>> getAllUnits(
            @RequestParam(required = false) Manufacturer manufacturer,
            @RequestParam(required = false)Element element,
            @RequestParam(required = false)Rarity rarity
            ){
        if (manufacturer != null) {
            return ResponseEntity.ok(unitRepository.findByManufacturer(manufacturer));
        }
        if (element != null) {
            return ResponseEntity.ok(unitRepository.findByElement(element));
        }
        if (rarity != null) {
            return ResponseEntity.ok(unitRepository.findByRarity(rarity));
        }
        return ResponseEntity.ok(unitRepository.findAll());
    }

    @GetMapping("/{unitCode}")
    public ResponseEntity<NikkeUnit> getUnitByCode(@PathVariable String unitCode) {
        return unitRepository.findByUnitCode(unitCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
