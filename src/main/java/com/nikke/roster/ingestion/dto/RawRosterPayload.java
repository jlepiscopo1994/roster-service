package com.nikke.roster.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawRosterPayload(
        @JsonProperty("NIKKECharacterData")List<RawCharacterNode> characters
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawCharacterNode(
            String slug,
            String name,
            String rarity,
            String element,
            RawCharacterData data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawCharacterData(
            String name,
            String slug,
            String rarity,
            @JsonProperty("burst_type") String burstType,
            String weapon,
            String element,
            @JsonProperty("class") String classType,
            String manufacturer,
            String backstory,
            List<String> specialities,
            @JsonProperty("card_image") String cardImage,
            @JsonProperty("full_image") String fullImage,
            @JsonProperty("ammo_capacity") Integer ammoCapacity,
            @JsonProperty("reload_time") Double reloadTime
    ){}
}
