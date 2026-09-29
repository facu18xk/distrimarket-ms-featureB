package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.*;
import com.distrimarket.commons.enums.TipoAjuste;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.AjusteStockDetalleMapper;
import com.distrimarket.inventario.mapper.AjusteStockMapper;
import com.distrimarket.inventario.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AjusteStockServiceImpl
        extends BaseServiceImpl<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO>
        implements AjusteStockService {

    private final AjusteStockRepository ajusteStockRepository;
    private final DepositoRepository depositoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoRepository productoRepository;
    private final StockDepositoRepository stockDepositoRepository;
    private final AjusteStockDetalleMapper detalleMapper;

    public AjusteStockServiceImpl(AjusteStockRepository ajusteStockRepository,
                                  AjusteStockMapper ajusteStockMapper,
                                  DepositoRepository depositoRepository,
                                  EmpleadoRepository empleadoRepository,
                                  ProductoRepository productoRepository,
                                  StockDepositoRepository stockDepositoRepository,
                                  AjusteStockDetalleMapper detalleMapper) {
        super(ajusteStockRepository, ajusteStockMapper);
        this.ajusteStockRepository = ajusteStockRepository;
        this.depositoRepository = depositoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.stockDepositoRepository = stockDepositoRepository;
        this.detalleMapper = detalleMapper;
    }

    @Override
    @Transactional
    public AjusteStockResponseDTO create(AjusteStockRequestDTO dto) {
        validateRequest(dto);

        Deposito deposito = depositoRepository.findById(dto.getIdDeposito())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado con ID: " + dto.getIdDeposito()));

        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + dto.getIdEmpleado()));

        if (!Boolean.TRUE.equals(empleado.getEstado())) {
            throw new IllegalArgumentException("El empleado asignado no se encuentra activo.");
        }

        AjusteStock ajuste = mapper.toEntity(dto);
        ajuste.setDeposito(deposito);
        ajuste.setEmpleado(empleado);

        // Procesar detalles y actualizar stock en depósito de forma atómica
        for (AjusteStockDetalleRequestDTO detDto : dto.getDetalles()) {
            if (detDto.getCantidad() == null || detDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad en cada detalle debe ser mayor a cero.");
            }

            Producto producto = productoRepository.findById(detDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + detDto.getIdProducto()));

            AjusteStockDetalle detalleEntity = detalleMapper.toEntity(detDto);
            detalleEntity.setProducto(producto);
            ajuste.addDetalle(detalleEntity);

            // Actualizar inventario en la tabla stock_deposito
            updateStockDeposito(deposito, producto, detDto.getCantidad(), dto.getTipoAjuste());
        }

        AjusteStock saved = ajusteStockRepository.save(ajuste);
        return mapper.toDTO(saved);
    }

    private void updateStockDeposito(Deposito deposito, Producto producto, Integer cantidad, TipoAjuste tipo) {
        StockDeposito stock = stockDepositoRepository.findByDepositoIdAndProductoId(deposito.getId(), producto.getId())
                .orElseGet(() -> StockDeposito.builder()
                        .deposito(deposito)
                        .producto(producto)
                        .cantidad(0)
                        .build());

        if (tipo == TipoAjuste.POSITIVO) {
            stock.setCantidad(stock.getCantidad() + cantidad);
        } else if (tipo == TipoAjuste.NEGATIVO) {
            if (stock.getCantidad() < cantidad) {
                throw new IllegalArgumentException(String.format(
                        "Stock insuficiente para el producto '%s' (ID %d). Disponible: %d, Solicitado descontar: %d",
                        producto.getNombre(), producto.getId(), stock.getCantidad(), cantidad));
            }
            stock.setCantidad(stock.getCantidad() - cantidad);
        }

        stockDepositoRepository.save(stock);
    }

    private void validateRequest(AjusteStockRequestDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El ajuste debe contener al menos un detalle de producto.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AjusteStockResponseDTO> findAllByDepositoId(Long depositoId, Pageable pageable) {
        return ajusteStockRepository.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    public AjusteStockResponseDTO update(Long id, AjusteStockRequestDTO createDTO) {
        throw new UnsupportedOperationException("Operación no permitida: Los ajustes de stock son inmutables.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Operación no permitida: No se pueden eliminar registros históricos de ajuste de stock.");
    }
}