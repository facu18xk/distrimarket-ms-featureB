package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.BaseEntity;
import com.distrimarket.commons.service.BusquedaTexto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseSpecification<E extends BaseEntity> {
    // Filtro base para entidades SoftDeletable
    public Specification<E> isActivo() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("activo"));
    }

    // Permite al frontend consultar inactivos si envía explícitamente ?activo=false
    public Specification<E> hasActivo(Boolean activo) {
        return (root, query, criteriaBuilder) -> {
            if (activo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("activo"), activo);
        };
    }

    public Specification<E> hasRelationId(String relation, Long id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction(); // Equivalente a un "WHERE 1=1" (ignora el filtro)
            }
            return criteriaBuilder.equal(root.get(relation).get("id"), id);
        };
    }

    public Specification<E> fieldEquals(String fieldName, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get(fieldName), value);
        };
    }

    /**
     * Búsqueda por texto (parámetro q): cada palabra tiene que aparecer en alguno de los campos,
     * sin importar mayúsculas ni el orden ("leche 1L" encuentra "Leche Entera 1L").
     * Usa la misma regla que el resto de los microservicios ({@link BusquedaTexto}).
     */
    public Specification<E> searchByText(String q, String... fieldNames) {
        return searchByNestedText(q, fieldNames);
    }

    /**
     * Igual que {@link #searchByText}, con campos de relaciones ("deposito.nombre"). Las relaciones
     * se unen con LEFT JOIN: una fila sin esa relación no desaparece de la búsqueda.
     */
    public Specification<E> searchByNestedText(String q, String... paths) {
        Specification<E> busqueda = BusquedaTexto.en(q, paths == null ? new String[0] : paths);
        return (root, query, cb) -> {
            Predicate predicado = busqueda.toPredicate(root, query, cb);
            return predicado == null ? cb.conjunction() : predicado;
        };
    }

    /**
     * Búsqueda híbrida: si 'q' coincide con un prefijo de código (ej: "TRA-0012", "AJU-5")
     * o es un número puro, evalúa el ID de la entidad. Además, ejecuta la búsqueda
     * textual sobre los campos anidados indicados.
     *
     * @param q término de búsqueda ingresado por el usuario
     * @param prefijo prefijo esperado para el código (ej: "TRA", "AJU")
     * @param nestedPaths atributos o rutas anidadas para la búsqueda de texto
     */
    public Specification<E> searchWithCodePrefix(String q, String prefijo, String... nestedPaths) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            String limpio = q.trim();
            Long idBuscado = extraerIdCodigo(limpio, prefijo);

            //Si coincide explícitamente con el patrón del prefijo (ej: TRA-0012 o aju-45)
            if (idBuscado != null && esPatronCodigo(limpio, prefijo)) {
                return cb.equal(root.get("id"), idBuscado);
            }

            //Búsqueda por texto en las propiedades anidadas
            Specification<E> textoSpec = searchByNestedText(limpio, nestedPaths);
            Predicate textoPredicate = textoSpec.toPredicate(root, query, cb);

            //Si era un número puro (ej: "12"), une coincidencia de ID con texto (OR)
            if (idBuscado != null) {
                Predicate idPredicate = cb.equal(root.get("id"), idBuscado);
                return textoPredicate != null ? cb.or(idPredicate, textoPredicate) : idPredicate;
            }

            return textoPredicate != null ? textoPredicate : cb.conjunction();
        };
    }

    /**
     * Comprueba si el texto comienza con el prefijo esperado con o sin guión (ej: "TRA-" o "TRA").
     */
    public boolean esPatronCodigo(String q, String prefijo) {
        if (q == null || prefijo == null) {
            return false;
        }
        String s = q.trim().toUpperCase();
        String p = prefijo.trim().toUpperCase();
        return s.startsWith(p + "-") || s.startsWith(p);
    }

    /**
     * Extrae el ID numérico descartando el prefijo y los ceros a la izquierda.
     * Funciona tanto para "TRA-0045", "tra45" como para "45".
     */
    public Long extraerIdCodigo(String q, String prefijo) {
        if (q == null || q.isBlank()) {
            return null;
        }

        String limpio = q.trim().toUpperCase();
        String p = prefijo != null ? prefijo.trim().toUpperCase() : "";

        if (!p.isEmpty()) {
            if (limpio.startsWith(p + "-")) {
                limpio = limpio.substring(p.length() + 1);
            } else if (limpio.startsWith(p)) {
                limpio = limpio.substring(p.length());
            }
        }

        limpio = limpio.replaceFirst("^0+", "");

        if (!limpio.isEmpty() && limpio.matches("\\d+")) {
            try {
                return Long.valueOf(limpio);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }
}
