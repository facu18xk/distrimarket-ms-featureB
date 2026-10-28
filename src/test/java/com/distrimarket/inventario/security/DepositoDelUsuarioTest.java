package com.distrimarket.inventario.security;

import com.distrimarket.commons.security.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepositoDelUsuarioTest {

    private static void sesion(Long idDeposito, String rol) {
        Jwt.Builder jwt = Jwt.withTokenValue("t").header("alg", "HS256").subject("prueba");
        if (idDeposito != null) {
            jwt.claim(JwtClaims.DEPOSITO_ID, idDeposito);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt.build(), List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void depositeroNoPuedeTocarStockDeOtroDeposito() {
        sesion(1L, "DEPOSITERO");

        assertThatThrownBy(() -> DepositoDelUsuario.exigirPropio(2L)).isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> DepositoDelUsuario.exigirPropio(1L)).doesNotThrowAnyException();
        assertThat(DepositoDelUsuario.resolver(2L)).isEqualTo(1L);
    }

    @Test
    void adminPuedeOperarEnCualquierDeposito() {
        sesion(1L, "ADMIN");

        assertThatCode(() -> DepositoDelUsuario.exigirPropio(2L)).doesNotThrowAnyException();
        assertThat(DepositoDelUsuario.resolver(2L)).isEqualTo(2L);
    }

    @Test
    void empleadoSinDepositoAsignadoNoQuedaBloqueado() {
        sesion(null, "DEPOSITERO");

        assertThatCode(() -> DepositoDelUsuario.exigirPropio(2L)).doesNotThrowAnyException();
        assertThat(DepositoDelUsuario.resolver(2L)).isEqualTo(2L);
    }
}
