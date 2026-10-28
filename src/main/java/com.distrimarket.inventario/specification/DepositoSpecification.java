package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.Deposito;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class DepositoSpecification {

    public Specification<Deposito> searchByText(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), pattern),
                    cb.like(cb.lower(root.get("ubicacion")), pattern)
            );
        };
    }
}