package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.*;
import com.distrimarket.commons.enums.TipoAjuste;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.AjusteStockMapper;
import com.distrimarket.inventario.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AjusteStockServiceImpl extends BaseServiceImpl<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> implements AjusteStockService {

    private final AjusteStockRepository ajusteStockRepository;
    private final DepositoRepository depositoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoRepository productoRepository;
    private final StockDepositoRepository stockDepositoRepository;
    private final AjusteStockMapper ajusteStockMapper;

    public AjusteStockServiceImpl(
            AjusteStockRepository ajusteStockRepository,
            AjusteStockMapper ajusteStockMapper,
            DepositoRepository depositoRepository,
            EmpleadoRepository empleadoRepository,
            ProductoRepository productoRepository,
            StockDepositoRepository stockDepositoRepository) {
        super(ajusteStockRepository, ajusteStockMapper);
        this.ajusteStockRepository = ajusteStockRepository;
        this.ajusteStockMapper = ajusteStockMapper;
        this.depositoRepository = depositoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.stockDepositoRepository = stockDepositoRepository;
    }

    @Override
    @Transactional
    public AjusteStockResponseDTO create(AjusteStockRequestDTO createDTO) {
        // 1. Buscar depósito y empleado
        Deposito deposito = depositoRepository.findById(createDTO.getIdDeposito())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado con ID: " + createDTO.getIdDeposito()));

        Empleado empleado = empleadoRepository.findById(createDTO.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + createDTO.getIdEmpleado()));

        // 2. Mapear DTO a la entidad de cabecera y asignar relaciones
        AjusteStock ajusteStock = ajusteStockMapper.toEntity(createDTO);
        ajusteStock.setDeposito(deposito);
        ajusteStock.setEmpleado(empleado);

        // 3. Procesar los detalles e impactar en el inventario (StockDeposito)
        if (createDTO.getDetalles() != null) {
            for (AjusteStockDetalleRequestDTO detalleDTO : createDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + detalleDTO.getIdProducto()));

                // Buscar el registro de stock actual en el depósito o inicializarlo en 0
                StockDeposito stockDeposito = ((Optional<StockDeposito>) stockDepositoRepository
                        .findByDepositoIdAndProductoId(deposito.getId(), producto.getId()))
                        .orElseGet(() -> {
                            StockDeposito nuevo = new StockDeposito();
                            nuevo.setDeposito(deposito);
                            nuevo.setProducto(producto);
                            nuevo.setCantidad(0);
                            return nuevo;
                        });

                // Actualizar la cantidad según el tipo de ajuste
                if (createDTO.getTipoAjuste() == TipoAjuste.POSITIVO) {
                    stockDeposito.setCantidad(stockDeposito.getCantidad() + detalleDTO.getCantidad());
                } else if (createDTO.getTipoAjuste() == TipoAjuste.NEGATIVO) {
                    if (stockDeposito.getCantidad() < detalleDTO.getCantidad()) {
                        throw new BadRequestException("Stock insuficiente para el producto '" + producto.getNombre()
                                + "'. Stock disponible: " + stockDeposito.getCantidad()
                                + ", cantidad a restar: " + detalleDTO.getCantidad());
                    }
                    stockDeposito.setCantidad(stockDeposito.getCantidad() - detalleDTO.getCantidad());
                }

                // Guardar la actualización del stock
                stockDepositoRepository.save(stockDeposito);

                // Crear el detalle de ajuste
                AjusteStockDetalle detalle = ajusteStockMapper.toDetalleEntity(detalleDTO);
                detalle.setProducto(producto);

                // Asignación bidireccional mediante el helper de la entidad
                ajusteStock.addDetalle(detalle);
            }
        }

        // 4. Guardar la entidad (por CascadeType.ALL se guardarán también los detalles)
        AjusteStock savedEntity = ajusteStockRepository.save(ajusteStock);

        return ajusteStockMapper.toDTO(savedEntity);
    }

    @Override
    @Transactional
    public AjusteStockResponseDTO update(Long id, AjusteStockRequestDTO updateDTO) {
        // Los ajustes de stock procesados no deberían ser editables para no alterar la auditoría de inventario
        throw new BadRequestException("Un ajuste de stock registrado no puede ser modificado.");
    }
}