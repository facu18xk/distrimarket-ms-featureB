package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Component
public class AjusteStockSpecification extends BaseSpecification<AjusteStock> {

    public Specification<AjusteStock> hasDepositoId(Long depositoId) {
        return hasRelationId("deposito", depositoId);
    }
    public Specification<AjusteStock> hasEmpleadoId(Long empleadoId) {
        return hasRelationId("empleado", empleadoId);
    }
    //Método QueryText anterior
    /*public Specification<AjusteStock> search(String q) {
        return searchByNestedText(q, "motivo", "deposito.nombre", "empleado.persona.nombreCompleto");
    }*/
    public Specification<AjusteStock> search(String q) {
        return searchWithCodePrefix(
                q,
                "AJU",
                "motivo",
                "deposito.nombre",
                "empleado.persona.nombreCompleto"
        );
    }
}