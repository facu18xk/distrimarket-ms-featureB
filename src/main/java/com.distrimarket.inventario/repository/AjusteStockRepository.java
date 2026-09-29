package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjusteStockRepository extends BaseRepository<AjusteStock> {

    @Override
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona", "detalles", "detalles.producto"})
    Optional<AjusteStock> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona"})
    Page<AjusteStock> findAll(Pageable pageable);
}