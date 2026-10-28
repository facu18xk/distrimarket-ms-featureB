package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.BaseEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

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

    public Specification<E> searchByText(String q, String... fieldNames) {
        return (root, query, criteriaBuilder) -> {
            if (q == null || q.isBlank() || fieldNames == null || fieldNames.length == 0) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + q.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String fieldName : fieldNames) {
                try {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(fieldName).as(String.class)),
                            pattern
                    ));
                } catch (IllegalArgumentException ex) {
                    // Previene caídas si un atributo no existe en la entidad evaluada
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
