package com.nikke.roster.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtRoleConverterTest {

    private KeycloakJwtRoleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakJwtRoleConverter();
    }

    @Test
    @DisplayName("Should extract and prefix realm roles from realm_access claim")
    void shouldExtractRolesFromJwt() {
        Jwt jwt = new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "user-123",
                        "realm_access", Map.of("roles", List.of("ROLE_CENTRAL_GOVERNMENT", "COMMANDER"))
                )
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CENTRAL_GOVERNMENT", "ROLE_COMMANDER");
    }

    @Test
    @DisplayName("Should return empty collection if realm_access claim is missing")
    void shouldReturnEmptyWhenNoRealmAccess() {
        Jwt jwt = new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "user-123")
        );

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).isEmpty();
    }
}