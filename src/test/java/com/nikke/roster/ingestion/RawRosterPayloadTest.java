package com.nikke.roster.ingestion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikke.roster.ingestion.dto.RawRosterPayload.RawCharacterNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RawRosterPayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should successfully deserialize NKAS seed file into character records")
    void shouldDeserializeNikkeRosterSeedFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/nikke-roster-seed.json");
        assertThat(resource.exists())
                .as("Seed file must exist at src/main/resources/data/nikke-roster-seed.json")
                .isTrue();

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            List<RawCharacterNode> characters;

            if (rootNode.isArray()) {
                characters = objectMapper.convertValue(
                        rootNode,
                        new TypeReference<List<RawCharacterNode>>() {}
                );
            } else if (rootNode.has("NIKKECharacterData")) {
                characters = objectMapper.convertValue(
                        rootNode.get("NIKKECharacterData"),
                        new TypeReference<List<RawCharacterNode>>() {}
                );
            } else {
                throw new IllegalStateException("Unrecognized JSON format in seed file.");
            }

            assertThat(characters).isNotNull().isNotEmpty();
            assertThat(characters.size()).isGreaterThanOrEqualTo(100);

            RawCharacterNode sampleNode = characters.stream()
                    .filter(node -> node != null && node.data() != null && node.data().name() != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No valid character node found in seed JSON"));

            assertThat(sampleNode.slug()).isNotBlank();
            assertThat(sampleNode.data().name()).isNotBlank();
            assertThat(sampleNode.data().manufacturer()).isNotBlank();
        }
    }
}