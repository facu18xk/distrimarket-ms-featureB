package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.commons.dto.StockMovimientoDetalleRequestDTO;
import com.distrimarket.commons.dto.StockMovimientoRequestDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.IllegalArgumentException;
import com.distrimarket.inventario.exception.InsufficientStockException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.StockDepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.repository.StockDepositoRepository;
import com.distrimarket.inventario.specification.StockDepositoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class StockDepositoServiceImpl
        extends BaseServiceImpl<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO>
        implements StockDepositoService {

    private final StockDepositoRepository stockDepositoRepository;
    private final DepositoRepository depositoRepository;
    private final ProductoRepository productoRepository;
    private final StockDepositoSpecification stockDepositoSpecification;

    public StockDepositoServiceImpl(StockDepositoRepository stockDepositoRepository,
                                    StockDepositoMapper stockDepositoMapper,
                                    DepositoRepository depositoRepository,
                                    ProductoRepository productoRepository,
                                    StockDepositoSpecification stockDepositoSpecification) {
        super(stockDepositoRepository, stockDepositoMapper);
        this.stockDepositoRepository = stockDepositoRepository;
        this.depositoRepository = depositoRepository;
        this.productoRepository = productoRepository;
        this.stockDepositoSpecification = stockDepositoSpecification;
    }

    @Override
    @Transactional
    public StockDepositoResponseDTO create(StockDepositoRequestDTO createDTO) {
        log.info("Creando registro de stock para depósito ID: {} y producto ID: {} con cantidad inicial: {}",
                createDTO.getIdDeposito(), createDTO.getIdProducto(), createDTO.getCantidad());
        validarExistenciaPadres(createDTO.getIdDeposito(), createDTO.getIdProducto());

        if (stockDepositoRepository.existsByDepositoIdAndProductoId(createDTO.getIdDeposito(), createDTO.getIdProducto())) {
            throw new DuplicateResourceException(
                    String.format("El producto con ID %d ya posee un registro de stock en el depósito con ID %d",
                            createDTO.getIdProducto(), createDTO.getIdDeposito())
            );
        }

        // Cargamos las entidades administradas para poblar los nombres en el ResponseDTO devuelto
        Deposito deposito = depositoRepository.findById(createDTO.getIdDeposito())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado"));
        Producto producto = productoRepository.findById(createDTO.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        StockDeposito entity = mapper.toEntity(createDTO);
        entity.setDeposito(deposito);
        entity.setProducto(producto);

        StockDeposito saved = stockDepositoRepository.save(entity);
        log.info("Registro de stock creado exitosamente con ID: {} (Depósito: '{}', Producto: '{}', Cantidad: {})",
                saved.getId(), deposito.getNombre(), producto.getNombre(), saved.getCantidad());
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public StockDepositoResponseDTO update(Long id, StockDepositoRequestDTO updateDTO) {
        log.info("Actualizando registro de stock con ID: {}", id);
        StockDeposito stockExistente = stockDepositoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de stock no encontrado con ID: " + id));

        validarExistenciaPadres(updateDTO.getIdDeposito(), updateDTO.getIdProducto());

        if (stockDepositoRepository.existsByDepositoIdAndProductoIdAndIdNot(
                updateDTO.getIdDeposito(), updateDTO.getIdProducto(), id)) {
            throw new DuplicateResourceException("Ya existe otro registro para este mismo depósito y producto.");
        }

        Deposito deposito = depositoRepository.findById(updateDTO.getIdDeposito())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado"));
        Producto producto = productoRepository.findById(updateDTO.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        mapper.updateEntityFromDto(updateDTO, stockExistente);
        stockExistente.setDeposito(deposito);
        stockExistente.setProducto(producto);

        StockDeposito updated = stockDepositoRepository.save(stockExistente);
        log.info("Registro de stock ID: {} actualizado exitosamente a cantidad: {}", id, updated.getCantidad());
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockDepositoResponseDTO> findAllWithSpecifications(Long depositoId, Long productoId, Pageable pageable) {
        log.debug("Consultando stock con filtros -> depositoId: {}, productoId: {}, página: {}, tamaño: {}",
                depositoId, productoId, pageable.getPageNumber(), pageable.getPageSize());
        Specification<StockDeposito> spec = Specification
                .where(stockDepositoSpecification.hasDepositoId(depositoId))
                .and(stockDepositoSpecification.hasProductoId(productoId));

        return stockDepositoRepository.findAll(spec, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StockDepositoResponseDTO findByDepositoIdAndProductoId(Long depositoId, Long productoId) {
        log.debug("Consultando stock puntual para depósito ID: {} y producto ID: {}", depositoId, productoId);
        StockDeposito stock = stockDepositoRepository.findByDepositoIdAndProductoId(depositoId, productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No existe registro de stock para el depósito %d y producto %d", depositoId, productoId)));
        return mapper.toDTO(stock);
    }

    @Override
    @Transactional
    public StockDeposito aplicarMovimiento(Deposito deposito, Producto producto, int cantidad) {
        if (cantidad == 0) {
            throw new IllegalArgumentException("La cantidad de un movimiento de stock no puede ser cero.");
        }
        StockDeposito stock = stockDepositoRepository
                .findWithLockByDepositoIdAndProductoId(deposito.getId(), producto.getId())
                .orElseGet(() -> StockDeposito.builder()
                        .deposito(deposito)
                        .producto(producto)
                        .cantidad(0)
                        .build());

        int cantidadAnterior = stock.getCantidad() == null ? 0 : stock.getCantidad();
        long cantidadResultante = (long) cantidadAnterior + cantidad;
        if (cantidadResultante < 0) {
            log.warn("Stock insuficiente para producto ID {} en depósito ID {}. Disponible: {}, Solicitado descontar: {}",
                    producto.getId(), deposito.getId(), cantidadAnterior, -cantidad);
            throw new InsufficientStockException(String.format(
                    "Stock insuficiente para el producto '%s' (ID %d). Disponible: %d, Solicitado descontar: %d",
                    producto.getNombre(), producto.getId(), cantidadAnterior, -cantidad));
        }
        if (cantidadResultante > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "El movimiento excede la cantidad máxima de stock permitida para el producto ID " + producto.getId());
        }

        stock.setCantidad((int) cantidadResultante);
        StockDeposito guardado = stockDepositoRepository.save(stock);
        log.debug("Stock actualizado para producto ID {} en depósito ID {}: {} -> {} (movimiento {})",
                producto.getId(), deposito.getId(), cantidadAnterior, cantidadResultante, cantidad);
        return guardado;
    }

    @Override
    @Transactional
    public List<StockDepositoResponseDTO> aplicarMovimientos(StockMovimientoRequestDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El movimiento debe contener al menos un detalle de producto.");
        }
        log.info("Aplicando movimiento de stock ({}) en depósito ID {} con {} detalle(s). Motivo: {}",
                dto.getOrigen(), dto.getIdDeposito(), dto.getDetalles().size(), dto.getMotivo());

        Deposito deposito = depositoRepository.findById(dto.getIdDeposito())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado con ID: " + dto.getIdDeposito()));

        // Se agrupa por producto y se recorre en orden de ID: mismo orden de bloqueo en toda operación.
        Map<Long, Integer> cantidadPorProducto = new TreeMap<>();
        for (StockMovimientoDetalleRequestDTO detalle : dto.getDetalles()) {
            if (detalle.getIdProducto() == null || detalle.getCantidad() == null || detalle.getCantidad() == 0) {
                throw new IllegalArgumentException("Cada detalle debe indicar un producto y una cantidad distinta de cero.");
            }
            try {
                cantidadPorProducto.merge(detalle.getIdProducto(), detalle.getCantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException(
                        "La cantidad total del producto ID " + detalle.getIdProducto() + " excede el rango permitido.");
            }
        }

        List<StockDepositoResponseDTO> resultado = new ArrayList<>();
        for (Map.Entry<Long, Integer> movimiento : cantidadPorProducto.entrySet()) {
            if (movimiento.getValue() == 0) {
                continue;
            }
            Producto producto = productoRepository.findById(movimiento.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + movimiento.getKey()));
            resultado.add(mapper.toDTO(aplicarMovimiento(deposito, producto, movimiento.getValue())));
        }
        log.info("Movimiento de stock aplicado en depósito ID {}: {} producto(s) afectados", deposito.getId(), resultado.size());
        return resultado;
    }

    private void validarExistenciaPadres(Long depositoId, Long productoId) {
        log.debug("Verificando existencia de entidades padre -> Depósito ID: {}, Producto ID: {}", depositoId, productoId);
        if (!depositoRepository.existsById(depositoId)) {
            throw new ResourceNotFoundException("No existe el depósito con ID: " + depositoId);
        }
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("No existe el producto con ID: " + productoId);
        }
    }
}