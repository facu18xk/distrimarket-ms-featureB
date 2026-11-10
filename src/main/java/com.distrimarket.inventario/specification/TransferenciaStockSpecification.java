package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.TransferenciaStock;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

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
    //Método de QueryText anterior
    /*public Specification<TransferenciaStock> search(String q) {
        return searchByNestedText(q, "depositoOrigen.nombre", "depositoDestino.nombre", "empleado.persona.nombreCompleto");
    }*/
    public Specification<TransferenciaStock> search(String q) {
        return searchWithCodePrefix(
                q,
                "TRA",
                "depositoOrigen.nombre",
                "depositoDestino.nombre",
                "empleado.persona.nombreCompleto"
        );
    }
}