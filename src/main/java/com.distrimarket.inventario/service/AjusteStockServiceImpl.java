package com.distrimarket.inventario.service;

import com.distrimarket.commons.service.BusquedaTexto;
import com.distrimarket.commons.service.Paginas;
import com.distrimarket.inventario.security.DepositoDelUsuario;
import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.*;
import com.distrimarket.commons.enums.TipoAjuste;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.exception.IllegalArgumentException;
import com.distrimarket.inventario.mapper.AjusteStockDetalleMapper;
import com.distrimarket.inventario.mapper.AjusteStockMapper;
import com.distrimarket.inventario.repository.*;
import com.distrimarket.inventario.specification.AjusteStockSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AjusteStockServiceImpl
        extends BaseServiceImpl<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO>
        implements AjusteStockService {

    private final AjusteStockRepository ajusteStockRepository;
    private final DepositoRepository depositoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProductoRepository productoRepository;
    private final StockDepositoService stockDepositoService;
    private final AjusteStockDetalleMapper detalleMapper;
    private final AjusteStockSpecification ajusteStockSpecification;

    public AjusteStockServiceImpl(AjusteStockRepository ajusteStockRepository,
                                  AjusteStockMapper ajusteStockMapper,
                                  DepositoRepository depositoRepository,
                                  EmpleadoRepository empleadoRepository,
                                  ProductoRepository productoRepository,
                                  StockDepositoService stockDepositoService,
                                  AjusteStockDetalleMapper detalleMapper,
                                  AjusteStockSpecification ajusteStockSpecification) {
        super(ajusteStockRepository, ajusteStockMapper);
        this.ajusteStockRepository = ajusteStockRepository;
        this.depositoRepository = depositoRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.stockDepositoService = stockDepositoService;
        this.detalleMapper = detalleMapper;
        this.ajusteStockSpecification = ajusteStockSpecification;
    }

    @Override
    @Transactional
    public AjusteStockResponseDTO create(AjusteStockRequestDTO dto) {
        dto.setIdDeposito(DepositoDelUsuario.resolver(dto.getIdDeposito()));
        log.info("Iniciando ajuste de stock [{}] para depósito ID {} por empleado ID {} con {} detalle(s)",
                dto.getTipoAjuste(), dto.getIdDeposito(), dto.getIdEmpleado(),
                dto.getDetalles() != null ? dto.getDetalles().size() : 0);
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
        ajuste.setActivo(true);

        // Validar cantidades primero y cargar todos los productos en una sola consulta SQL (WHERE id IN (...)),
        // evitando N llamadas a productoRepository.findById dentro del bucle.
        for (AjusteStockDetalleRequestDTO detDto : dto.getDetalles()) {
            if (detDto.getCantidad() == null || detDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad en cada detalle debe ser mayor a cero.");
            }
        }

        Set<Long> idsProducto = dto.getDetalles().stream()
                .map(AjusteStockDetalleRequestDTO::getIdProducto)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Producto> productosPorId = productoRepository.findAllById(idsProducto).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        // Procesar detalles y actualizar stock en depósito de forma atómica
        for (AjusteStockDetalleRequestDTO detDto : dto.getDetalles()) {
            Producto producto = productosPorId.get(detDto.getIdProducto());
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado con ID: " + detDto.getIdProducto());
            }

            AjusteStockDetalle detalleEntity = detalleMapper.toEntity(detDto);
            detalleEntity.setProducto(producto);
            ajuste.addDetalle(detalleEntity);

            // Actualizar inventario en la tabla stock_deposito
            updateStockDeposito(deposito, producto, detDto.getCantidad(), dto.getTipoAjuste());
        }

        AjusteStock saved = ajusteStockRepository.save(ajuste);
        log.info("Ajuste de stock registrado exitosamente con ID: {}", saved.getId());
        return mapper.toDTO(saved);
    }

    private void updateStockDeposito(Deposito deposito, Producto producto, Integer cantidad, TipoAjuste tipo) {
        // La regla de stock (bloqueo de fila y validación de existencias) vive en StockDepositoService.
        if (tipo == TipoAjuste.POSITIVO) {
            stockDepositoService.aplicarMovimiento(deposito, producto, cantidad);
        } else if (tipo == TipoAjuste.NEGATIVO) {
            stockDepositoService.aplicarMovimiento(deposito, producto, -cantidad);
        }
    }

    private void validateRequest(AjusteStockRequestDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El ajuste debe contener al menos un detalle de producto.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AjusteStockResponseDTO> findAllByDepositoId(Long depositoId, Pageable pageable) {
        log.debug("Consultando ajustes de stock para depósito ID: {}, página: {}", depositoId, pageable.getPageNumber());
        return ajusteStockRepository.findByDepositoId(depositoId, pageable).map(mapper::toDTO);
    }

    @Override
    public AjusteStockResponseDTO update(Long id, AjusteStockRequestDTO createDTO) {
        throw new UnsupportedOperationException("Operación no permitida: Los ajustes de stock son inmutables.");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Iniciando anulación (borrado lógico) del ajuste de stock con ID: {}", id);

        AjusteStock ajuste = ajusteStockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ajuste de stock no encontrado con ID: " + id));

        if (Boolean.FALSE.equals(ajuste.getActivo())) {
            throw new IllegalArgumentException("El ajuste de stock con ID " + id + " ya se encuentra anulado.");
        }

        // Rollback compensatorio sobre el stock en depósito
        for (AjusteStockDetalle detalle : ajuste.getDetalles()) {
            Producto producto = detalle.getProducto();
            Integer cantidad = detalle.getCantidad();

            // Si el ajuste original sumó stock (POSITIVO), la anulación lo descuenta (-cantidad).
            // Si el ajuste original restó stock (NEGATIVO), la anulación lo devuelve (+cantidad).
            if (ajuste.getTipoAjuste() == TipoAjuste.POSITIVO) {
                log.info("Revirtiendo ajuste positivo: descontando {} unidades de producto ID {} en depósito ID {}",
                        cantidad, producto.getId(), ajuste.getDeposito().getId());
                stockDepositoService.aplicarMovimiento(ajuste.getDeposito(), producto, -cantidad);
            } else if (ajuste.getTipoAjuste() == TipoAjuste.NEGATIVO) {
                log.info("Revirtiendo ajuste negativo: reincorporando {} unidades de producto ID {} en depósito ID {}",
                        cantidad, producto.getId(), ajuste.getDeposito().getId());
                stockDepositoService.aplicarMovimiento(ajuste.getDeposito(), producto, cantidad);
            }
        }

        // Marcar el registro como inactivo
        ajuste.setActivo(false);
        ajusteStockRepository.save(ajuste);
        log.info("Ajuste de stock ID: {} anulado exitosamente.", id);
    }

    @Override
    public void reactivarById(Long id) {
        throw new UnsupportedOperationException("No está permitido reactivar un ajuste de stock anulado. Registre un nuevo ajuste si corresponde.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AjusteStockResponseDTO> findAllWithSpecifications(String q, Long depositoId, Long empleadoId, Pageable pageable) {
        log.debug("Consultando ajustes con filtros -> depósitoId: {}, empleadoId: {}, página: {}",
                depositoId, empleadoId, pageable.getPageNumber());
        Specification<AjusteStock> spec = Specification
                .where(ajusteStockSpecification.search(q))
                .and(ajusteStockSpecification.hasDepositoId(depositoId))
                .and(ajusteStockSpecification.hasEmpleadoId(empleadoId));

        // Desempate por id: hay ajustes con la misma fechaCreacion y sin él las páginas se repiten.
        return ajusteStockRepository.findAll(spec.and(BusquedaTexto.en(q, "motivo", "deposito.nombre", "empleado.persona.nombreCompleto")), Paginas.conDesempatePorId(pageable)).map(mapper::toDTO);
    }
}