package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.TransferenciaStockDetalleRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockResponseDTO;
import com.distrimarket.commons.entity.*;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.exception.InsufficientStockException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.TransferenciaStockDetalleMapper;
import com.distrimarket.inventario.mapper.TransferenciaStockMapper;
import com.distrimarket.inventario.repository.*;
import com.distrimarket.inventario.specification.TransferenciaStockSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransferenciaStockServiceImpl
        extends BaseServiceImpl<TransferenciaStock, TransferenciaStockRequestDTO, TransferenciaStockResponseDTO>
        implements TransferenciaStockService {

    private final TransferenciaStockRepository transferenciaStockRepository;
    private final DepositoRepository depositoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoRepository productoRepository;
    private final StockDepositoService stockDepositoService;
    private final TransferenciaStockDetalleMapper detalleMapper;
    private final TransferenciaStockSpecification specification;

    public TransferenciaStockServiceImpl(TransferenciaStockRepository transferenciaStockRepository,
                                         TransferenciaStockMapper transferenciaStockMapper,
                                         DepositoRepository depositoRepository,
                                         EmpleadoRepository empleadoRepository,
                                         ProductoRepository productoRepository,
                                         StockDepositoService stockDepositoService,
                                         TransferenciaStockDetalleMapper detalleMapper,
                                         TransferenciaStockSpecification specification) {
        super(transferenciaStockRepository, transferenciaStockMapper);
        this.transferenciaStockRepository = transferenciaStockRepository;
        this.depositoRepository = depositoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.stockDepositoService = stockDepositoService;
        this.detalleMapper = detalleMapper;
        this.specification = specification;
    }

    @Override
    @Transactional
    public TransferenciaStockResponseDTO create(TransferenciaStockRequestDTO dto) {
        log.info("Iniciando registro de transferencia de stock desde depósito {} hacia depósito {} por empleado ID {}",
                dto.getIdDepositoOrigen(), dto.getIdDepositoDestino(), dto.getIdEmpleado());
        validarRequest(dto);

        Deposito depositoOrigen = depositoRepository.findById(dto.getIdDepositoOrigen())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito origen no encontrado con ID: " + dto.getIdDepositoOrigen()));

        Deposito depositoDestino = depositoRepository.findById(dto.getIdDepositoDestino())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito destino no encontrado con ID: " + dto.getIdDepositoDestino()));

        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + dto.getIdEmpleado()));

        if (!Boolean.TRUE.equals(empleado.getEstado())) {
            throw new BadRequestException("El empleado asignado no se encuentra activo.");
        }

        TransferenciaStock transferencia = mapper.toEntity(dto);
        transferencia.setDepositoOrigen(depositoOrigen);
        transferencia.setDepositoDestino(depositoDestino);
        transferencia.setEmpleado(empleado);
        transferencia.setEstado("COMPLETADO");

        // Procesar detalles y mover existencias
        for (TransferenciaStockDetalleRequestDTO detDto : dto.getDetalles()) {
            log.debug("Procesando detalle: Producto ID {}, Cantidad {}", detDto.getIdProducto(), detDto.getCantidad());
            if (detDto.getCantidad() == null || detDto.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad a transferir debe ser mayor a cero.");
            }

            Producto producto = productoRepository.findById(detDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + detDto.getIdProducto()));

            TransferenciaStockDetalle detalle = detalleMapper.toEntity(detDto);
            detalle.setProducto(producto);
            transferencia.addDetalle(detalle);

            ejecutarMovimientoInventario(depositoOrigen, depositoDestino, producto, detDto.getCantidad());
        }

        TransferenciaStock guardado = transferenciaStockRepository.save(transferencia);
        log.info("Transferencia de stock registrada con éxito con ID: {}", guardado.getId());
        return mapper.toDTO(guardado);
    }

    private void ejecutarMovimientoInventario(Deposito origen, Deposito destino, Producto producto, Integer cantidad) {
        // 1. Validar y descontar del depósito de origen
        // La regla de stock (bloqueo de fila y validación de existencias) vive en StockDepositoService.
        log.debug("Descontando {} unidades del depósito {}", cantidad, origen.getId());
        try {
            stockDepositoService.aplicarMovimiento(origen, producto, -cantidad);
        } catch (InsufficientStockException ex) {
            log.warn("Fallo de transferencia desde el depósito origen ID {}: {}", origen.getId(), ex.getMessage());
            // Las transferencias siempre respondieron 400 ante stock insuficiente en el origen.
            throw new BadRequestException(ex.getMessage());
        }

        // 2. Acreditar en el depósito de destino
        stockDepositoService.aplicarMovimiento(destino, producto, cantidad);
    }

    private void validarRequest(TransferenciaStockRequestDTO dto) {
        if (dto.getIdDepositoOrigen().equals(dto.getIdDepositoDestino())) {
            throw new BadRequestException("El depósito de origen y el depósito de destino no pueden ser el mismo.");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BadRequestException("La transferencia debe incluir al menos un detalle de producto.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferenciaStockResponseDTO> findAllWithSpecifications(Long origenId, Long destinoId, Long empleadoId, Pageable pageable) {
        Specification<TransferenciaStock> spec = Specification
                .where(specification.hasDepositoOrigenId(origenId))
                .and(specification.hasDepositoDestinoId(destinoId))
                .and(specification.hasEmpleadoId(empleadoId));

        return transferenciaStockRepository.findAll(spec, pageable).map(mapper::toDTO);
    }

    @Override
    public TransferenciaStockResponseDTO update(Long id, TransferenciaStockRequestDTO createDTO) {
        throw new UnsupportedOperationException("Operación no permitida: Las transferencias de stock son inmutables.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Operación no permitida: No se pueden eliminar transferencias de stock registradas.");
    }
}