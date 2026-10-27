package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AjusteStockRepository extends BaseRepository<AjusteStock> {

    /**
     * @EntityGraph precarga en un único SELECT con LEFT JOIN el depósito, el empleado,
     * la persona del empleado, los detalles y el producto de cada detalle al buscar por ID,
     * evitando múltiples consultas LAZY adicionales.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona", "detalles", "detalles.producto"})
    Optional<AjusteStock> findById(Long id);

    /**
     * @EntityGraph precarga las relaciones @ManyToOne/@OneToOne de cabecera (depósito, empleado
     * y persona) en la misma consulta paginada. Los detalles (@OneToMany) y sus productos se cargan
     * en lote mediante hibernate.default_batch_fetch_size para mantener la paginación en SQL.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona"})
    Page<AjusteStock> findAll(Pageable pageable);

    /**
     * @EntityGraph para el listado paginado con filtros (Specification) usado por el controlador;
     * evita 3*N consultas LAZY para depósito, empleado y persona en cada página.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona"})
    Page<AjusteStock> findAll(Specification<AjusteStock> spec, Pageable pageable);

    /**
     * @EntityGraph precarga depósito, empleado y persona al filtrar ajustes por ID de depósito.
     */
    @EntityGraph(attributePaths = {"deposito", "empleado", "empleado.persona"})
    Page<AjusteStock> findByDepositoId(Long depositoId, Pageable pageable);
}