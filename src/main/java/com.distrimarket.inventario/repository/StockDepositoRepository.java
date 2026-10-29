package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.StockDeposito;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockDepositoRepository extends BaseRepository<StockDeposito> {
    boolean existsByDepositoIdAndProductoId(Long depositoId, Long productoId);
    boolean existsByDepositoIdAndProductoIdAndIdNot(Long depositoId, Long productoId, Long id);
    /** Cuenta solo existencias activas (la entidad filtra activo = true). */
    long countByDepositoId(Long depositoId);

    /**
     * @EntityGraph precarga en un solo SELECT con LEFT JOIN el depósito y el producto
     * necesarios para construir StockDepositoResponseDTO (nombreDeposito y nombreProducto),
     * evitando 2 consultas LAZY adicionales.
     */
    @EntityGraph(attributePaths = {"deposito", "producto"})
    Optional<StockDeposito> findByDepositoIdAndProductoId(Long depositoId, Long productoId);

    /**
     * @EntityGraph precarga depósito y producto al consultar un registro de stock por ID.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "producto"})
    Optional<StockDeposito> findById(Long id);

    /**
     * @EntityGraph precarga depósito y producto en el listado paginado general,
     * evitando 2*N consultas LAZY por página.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "producto"})
    Page<StockDeposito> findAll(Pageable pageable);

    /**
     * @EntityGraph precarga depósito y producto en el listado paginado con filtros (Specification),
     * resolviendo toda la página en una sola consulta SQL principal.
     */
    @Override
    @EntityGraph(attributePaths = {"deposito", "producto"})
    Page<StockDeposito> findAll(Specification<StockDeposito> spec, Pageable pageable);

    /**
     * @Lock(PESSIMISTIC_WRITE) emite SELECT ... FOR UPDATE para bloquear la fila hasta el fin
     * de la transacción y evitar condiciones de carrera entre movimientos simultáneos.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockDeposito> findWithLockByDepositoIdAndProductoId(Long depositoId, Long productoId);
}
