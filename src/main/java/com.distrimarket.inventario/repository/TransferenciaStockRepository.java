package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.TransferenciaStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferenciaStockRepository extends BaseRepository<TransferenciaStock> {

    @Override
    @EntityGraph(attributePaths = {
            "depositoOrigen",
            "depositoDestino",
            "empleado",
            "empleado.persona",
            "detalles",
            "detalles.producto"
    })
    Optional<TransferenciaStock> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"depositoOrigen", "depositoDestino", "empleado", "empleado.persona"})
    Page<TransferenciaStock> findAll(Specification<TransferenciaStock> spec, Pageable pageable);
}