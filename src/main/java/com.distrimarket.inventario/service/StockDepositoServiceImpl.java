package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.exception.DuplicateResourceException;
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