package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class AjusteStockSpecification extends BaseSpecification<AjusteStock> {

    public Specification<AjusteStock> hasDepositoId(Long depositoId) {
        return hasRelationId("deposito", depositoId);
    }

    public Specification<AjusteStock> hasEmpleadoId(Long empleadoId) {
        return hasRelationId("empleado", empleadoId);
    }
}