package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.inventario.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoSpecification extends BaseSpecification<Empleado> {
    public Specification<Empleado> withEstado(Boolean estado) {
        return fieldEquals("estado", estado);
    }
    public Specification<Empleado> withCargo(String cargo) {
        return fieldEquals("cargo", cargo);
    }
}
