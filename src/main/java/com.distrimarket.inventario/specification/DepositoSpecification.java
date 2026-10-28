package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.Deposito;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class DepositoSpecification extends BaseSpecification<Deposito>{

    public Specification<Deposito> search(String q) {
        return searchByText(q, "nombre", "ubicacion");
    }
}