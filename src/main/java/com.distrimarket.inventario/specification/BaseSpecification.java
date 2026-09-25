package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

public abstract class BaseSpecification<E extends BaseEntity> {
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
}
