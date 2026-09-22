package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.StockDeposito;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockDepositoRepository extends BaseRepository<StockDeposito> {

    // Comprueba si ya existe un registro para esa combinación de depósito y producto
    boolean existsByDepositoIdAndProductoId(Long idDeposito, Long idProducto);

    // Obtiene el registro de stock por la combinación de depósito y producto
    Optional findByDepositoIdAndProductoId(Long idDeposito, Long idProducto);
}