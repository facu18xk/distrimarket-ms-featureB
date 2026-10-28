package com.distrimarket.inventario.security;

import com.distrimarket.commons.security.UsuarioActual;
import org.springframework.security.access.AccessDeniedException;

/**
 * Regla de depósito por usuario: un ADMIN opera sobre cualquier depósito; el resto (depositero,
 * vendedor, comprador) solo sobre el depósito asignado en su token. Un empleado sin depósito
 * asignado sigue eligiendo (compatibilidad con usuarios creados antes de esta regla).
 */
public final class DepositoDelUsuario {

    private DepositoDelUsuario() {
    }

    /** Depósito con el que se opera: el pedido (ADMIN) o el propio del usuario. */
    public static Long resolver(Long pedido) {
        return UsuarioActual.depositoParaOperar(pedido);
    }

    /** Para corregir o quitar algo que ya existe: tiene que ser de su depósito. */
    public static void exigirPropio(Long idDeposito) {
        if (UsuarioActual.esAdmin()) {
            return;
        }
        UsuarioActual.idDeposito()
                .filter(propio -> !propio.equals(idDeposito))
                .ifPresent(propio -> {
                    throw new AccessDeniedException("Solo puede operar sobre el stock de su depósito.");
                });
    }
}
