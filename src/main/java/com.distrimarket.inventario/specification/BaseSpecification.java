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
}
