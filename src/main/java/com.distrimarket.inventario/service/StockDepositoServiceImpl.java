package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.mapper.StockDepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.repository.StockDepositoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.StockDepositoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockDepositoServiceImpl
        extends BaseServiceImpl<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO>
        implements StockDepositoService {

    private final StockDepositoRepository stockDepositoRepository;
    private final DepositoRepository depositoRepository;
    private final ProductoRepository productoRepository;
    //private final StockDepositoMapper stockDepositoMapper;

    public StockDepositoServiceImpl(
            StockDepositoRepository stockDepositoRepository,
            StockDepositoMapper stockDepositoMapper,
            DepositoRepository depositoRepository,
            ProductoRepository productoRepository) {
        super(stockDepositoRepository, stockDepositoMapper);
        this.stockDepositoRepository = stockDepositoRepository;
        this.depositoRepository = depositoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public StockDepositoResponseDTO create(StockDepositoRequestDTO createDTO) {
        // 1. Validar si ya existe el par Depósito - Producto
        if (stockDepositoRepository.existsByDepositoIdAndProductoId(createDTO.getIdDeposito(), createDTO.getIdProducto())) {
            throw new RuntimeException("Ya existe un registro de stock para el Producto ID "
                    + createDTO.getIdProducto() + " en el Depósito ID " + createDTO.getIdDeposito());
        }

        // 2. Buscar Entidades
        Deposito deposito = depositoRepository.findById(createDTO.getIdDeposito())
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado con ID: " + createDTO.getIdDeposito()));

        Producto producto = productoRepository.findById(createDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + createDTO.getIdProducto()));

        // 3. Convertir y asociar relaciones manualmente
        StockDeposito stockDeposito = mapper.toEntity(createDTO);
        stockDeposito.setDeposito(deposito);
        stockDeposito.setProducto(producto);

        StockDeposito savedEntity = stockDepositoRepository.save(stockDeposito);
        return mapper.toDTO(savedEntity);
    }

    @Override
    @Transactional
    public StockDepositoResponseDTO update(Long id, StockDepositoRequestDTO createDTO) {
        StockDeposito stockDeposito = stockDepositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de stock no encontrado con ID: " + id));

        // En actualización permitimos modificar principalmente la cantidad
        stockDeposito.setCantidad(createDTO.getCantidad());

        StockDeposito updatedEntity = stockDepositoRepository.save(stockDeposito);
        return mapper.toDTO(updatedEntity);
    }
}