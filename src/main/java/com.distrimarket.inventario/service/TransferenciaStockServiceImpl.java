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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        transferencia.setActivo(true);

        // Validar cantidades primero y cargar todos los productos de la transferencia en una sola
        // consulta SQL (WHERE id IN (...)), evitando N consultas individuales dentro del bucle.
        for (TransferenciaStockDetalleRequestDTO detDto : dto.getDetalles()) {
            if (detDto.getCantidad() == null || detDto.getCantidad() <= 0) {
                throw new BadRequestException("La cantidad a transferir debe ser mayor a cero.");
            }
        }

        Set<Long> idsProducto = dto.getDetalles().stream()
                .map(TransferenciaStockDetalleRequestDTO::getIdProducto)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Producto> productosPorId = productoRepository.findAllById(idsProducto).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        // Procesar detalles y mover existencias
        for (TransferenciaStockDetalleRequestDTO detDto : dto.getDetalles()) {
            log.debug("Procesando detalle: Producto ID {}, Cantidad {}", detDto.getIdProducto(), detDto.getCantidad());
            Producto producto = productosPorId.get(detDto.getIdProducto());
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado con ID: " + detDto.getIdProducto());
            }

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
    @Transactional
    public void deleteById(Long id) {
        log.info("Iniciando anulación (borrado lógico) de la transferencia de stock ID: {}", id);

        TransferenciaStock transferencia = transferenciaStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transferencia de stock no encontrada con ID: " + id));

        if (Boolean.FALSE.equals(transferencia.getActivo())) {
            throw new BadRequestException("La transferencia de stock con ID " + id + " ya se encuentra anulada.");
        }

        Deposito origen = transferencia.getDepositoOrigen();
        Deposito destino = transferencia.getDepositoDestino();

        // Rollback compensatorio para cada producto transferido
        for (TransferenciaStockDetalle detalle : transferencia.getDetalles()) {
            Producto producto = detalle.getProducto();
            Integer cantidad = detalle.getCantidad();

            log.debug("Revirtiendo transferencia: descontando {} unidades del depósito destino ID {} y reingresando al depósito origen ID {}",
                    cantidad, destino.getId(), origen.getId());

            // 1. Descontar del destino (falla con 400 si el destino ya consumió o vendió ese stock)
            try {
                stockDepositoService.aplicarMovimiento(destino, producto, -cantidad);
            } catch (InsufficientStockException ex) {
                log.warn("Fallo al revertir transferencia: stock insuficiente en depósito destino ID {}: {}", destino.getId(), ex.getMessage());
                throw new BadRequestException("No se puede anular la transferencia: el depósito destino no cuenta con stock suficiente para devolver el producto '"
                        + producto.getNombre() + "'.");
            }

            // 2. Acreditar de regreso en el origen
            stockDepositoService.aplicarMovimiento(origen, producto, cantidad);
        }

        // Marcar la transferencia como inactiva y actualizar estado
        transferencia.setActivo(false);
        transferencia.setEstado("ANULADO");
        transferenciaStockRepository.save(transferencia);

        log.info("Transferencia de stock ID: {} anulada con éxito.", id);
    }

    @Override
    public void reactivarById(Long id) {
        throw new UnsupportedOperationException("No se permite reactivar una transferencia anulada. Genere una nueva transferencia si corresponde.");
    }
}