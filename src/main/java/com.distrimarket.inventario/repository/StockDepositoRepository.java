package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.StockDeposito;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockDepositoRepository extends BaseRepository<StockDeposito> {
    boolean existsByDepositoIdAndProductoId(Long depositoId, Long productoId);
    boolean existsByDepositoIdAndProductoIdAndIdNot(Long depositoId, Long productoId, Long id);
    Optional<StockDeposito> findByDepositoIdAndProductoId(Long depositoId, Long productoId);
}