package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.StockDeposito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockDepositoRepository extends BaseRepository<StockDeposito> {
    boolean existsByDepositoIdAndProductoId(Long depositoId, Long productoId);
    boolean existsByDepositoIdAndProductoIdAndIdNot(Long depositoId, Long productoId, Long id);
    Optional<StockDeposito> findByDepositoIdAndProductoId(Long depositoId, Long productoId);

    // Bloquea la fila hasta el fin de la transacción para que dos movimientos simultáneos no se pisen.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockDeposito> findWithLockByDepositoIdAndProductoId(Long depositoId, Long productoId);
}
