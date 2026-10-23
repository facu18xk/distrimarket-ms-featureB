package com.distrimarket.inventario.config;

import com.distrimarket.commons.enums.RolUsuario;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        Jwt jwt,
        Password password,
        List<String> publicPaths,
        List<UrlRule> rules,
        BootstrapAdmin bootstrapAdmin) {

    private static final int MIN_SECRET_BYTES = 32;

    public SecurityProperties {
        if (jwt == null) {
            throw new IllegalStateException("Falta la configuración security.jwt.");
        }
        publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
        rules = rules == null ? List.of() : List.copyOf(rules);
        password = password == null ? new Password(10) : password;
    }

    public record Jwt(String secret, String issuer, Duration expiration) {
        public Jwt {
            if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
                throw new IllegalStateException(
                        "security.jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " caracteres.");
            }
            if (issuer == null || issuer.isBlank()) {
                throw new IllegalStateException("security.jwt.issuer es obligatorio.");
            }
            expiration = expiration == null ? Duration.ofHours(8) : expiration;
        }
    }

    public record Password(int bcryptStrength) {
    }

    public record UrlRule(String pattern, List<String> methods, List<RolUsuario> roles) {
        public UrlRule {
            methods = methods == null ? List.of() : List.copyOf(methods);
            roles = roles == null ? List.of() : List.copyOf(roles);
        }
    }

    public record BootstrapAdmin(
            boolean enabled, String username, String password, String nombreCompleto, String ci) {
    }
}