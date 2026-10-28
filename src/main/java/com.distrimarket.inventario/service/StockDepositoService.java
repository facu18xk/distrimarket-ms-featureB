package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.dto.StockMovimientoRequestDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.StockDeposito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockDepositoService extends BaseService<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {
    Page<StockDepositoResponseDTO> findAllWithSpecifications(Long depositoId, Long productoId, String q, Pageable pageable);
    StockDepositoResponseDTO findByDepositoIdAndProductoId(Long depositoId, Long productoId);

    /**
     * Única regla para mover existencias: suma si la cantidad es positiva y resta si es negativa.
     * La usan ajustes, transferencias y el endpoint de movimientos (ventas y compras).
     * Debe ejecutarse dentro de la transacción de quien la invoca.
     */
    StockDeposito aplicarMovimiento(Deposito deposito, Producto producto, int cantidad);

    /** Aplica varios movimientos sobre un depósito de forma atómica. */
    List<StockDepositoResponseDTO> aplicarMovimientos(StockMovimientoRequestDTO dto);
}
