package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.TransferenciaStock;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TransferenciaStockSpecification extends BaseSpecification<TransferenciaStock> {

    public Specification<TransferenciaStock> hasDepositoOrigenId(Long depositoOrigenId) {
        return hasRelationId("depositoOrigen", depositoOrigenId);
    }

    public Specification<TransferenciaStock> hasDepositoDestinoId(Long depositoDestinoId) {
        return hasRelationId("depositoDestino", depositoDestinoId);
    }

    public Specification<TransferenciaStock> hasEmpleadoId(Long empleadoId) {
        return hasRelationId("empleado", empleadoId);
    }
}