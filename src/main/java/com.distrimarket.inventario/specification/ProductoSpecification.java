package com.distrimarket.inventario.specification;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ProductoSpecification extends BaseSpecification<Producto> {

    public Specification<Producto> hasCategoriaId(Long categoriaId) {
        return hasRelationId("categoria", categoriaId);
    }

    public Specification<Producto> hasMarcaId(Long marcaId) {
        return hasRelationId("marca", marcaId);
    }

    public Specification<Producto> withEstado(Boolean estado) {
        return fieldEquals("estado", estado);
    }
}
