package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.StockDeposito;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class StockDepositoSpecification extends BaseSpecification<StockDeposito> {
    public Specification<StockDeposito> hasDepositoId(Long depositoId) {
        return hasRelationId("deposito", depositoId);
    }
    public Specification<StockDeposito> hasProductoId(Long productoId) {
        return hasRelationId("producto", productoId);
    }
}