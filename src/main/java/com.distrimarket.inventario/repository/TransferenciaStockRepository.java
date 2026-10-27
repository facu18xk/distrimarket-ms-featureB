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

    /**
     * @EntityGraph precarga en un único SELECT con LEFT JOIN los depósitos de origen y destino,
     * el empleado con su persona, las líneas de detalle y sus productos al consultar por ID.
     */
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

    /**
     * @EntityGraph precarga las relaciones de cabecera en el listado paginado sin filtros,
     * evitando 4*N consultas LAZY por página.
     */
    @Override
    @EntityGraph(attributePaths = {"depositoOrigen", "depositoDestino", "empleado", "empleado.persona"})
    Page<TransferenciaStock> findAll(Pageable pageable);

    /**
     * @EntityGraph precarga las relaciones de cabecera en el listado paginado con filtros (Specification).
     * La colección "detalles" y sus productos se cargan en lote vía hibernate.default_batch_fetch_size
     * para no forzar paginación en memoria (HHH90003004).
     */
    @Override
    @EntityGraph(attributePaths = {"depositoOrigen", "depositoDestino", "empleado", "empleado.persona"})
    Page<TransferenciaStock> findAll(Specification<TransferenciaStock> spec, Pageable pageable);
}