package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockDetalleRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockRequestDTO;
import com.distrimarket.commons.entity.AjusteStock;
import com.distrimarket.commons.entity.AjusteStockDetalle;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.TransferenciaStock;
import com.distrimarket.commons.entity.TransferenciaStockDetalle;
import com.distrimarket.commons.enums.TipoAjuste;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.exception.InsufficientStockException;
import com.distrimarket.inventario.mapper.AjusteStockDetalleMapper;
import com.distrimarket.inventario.mapper.AjusteStockMapper;
import com.distrimarket.inventario.mapper.TransferenciaStockDetalleMapper;
import com.distrimarket.inventario.mapper.TransferenciaStockMapper;
import com.distrimarket.inventario.repository.AjusteStockRepository;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.repository.EmpleadoRepository;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.repository.TransferenciaStockRepository;
import com.distrimarket.inventario.specification.AjusteStockSpecification;
import com.distrimarket.inventario.specification.TransferenciaStockSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ajustes y transferencias delegan el cambio de existencias en la regla única de StockDepositoService. */
@ExtendWith(MockitoExtension.class)
class MovimientosDeStockReutilizadosTest {

    @Mock private StockDepositoService stockDepositoService;
    @Mock private DepositoRepository depositoRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private ProductoRepository productoRepository;

    @Mock private AjusteStockRepository ajusteStockRepository;
    @Mock private AjusteStockMapper ajusteStockMapper;
    @Mock private AjusteStockDetalleMapper ajusteDetalleMapper;
    @Mock private AjusteStockSpecification ajusteStockSpecification;

    @Mock private TransferenciaStockRepository transferenciaStockRepository;
    @Mock private TransferenciaStockMapper transferenciaStockMapper;
    @Mock private TransferenciaStockDetalleMapper transferenciaDetalleMapper;
    @Mock private TransferenciaStockSpecification transferenciaStockSpecification;

    private AjusteStockServiceImpl ajusteService;
    private TransferenciaStockServiceImpl transferenciaService;
    private Deposito central;
    private Deposito sucursal;
    private Producto leche;

    @BeforeEach
    void setUp() {
        ajusteService = new AjusteStockServiceImpl(ajusteStockRepository, ajusteStockMapper, depositoRepository,
                empleadoRepository, productoRepository, stockDepositoService, ajusteDetalleMapper,
                ajusteStockSpecification);
        transferenciaService = new TransferenciaStockServiceImpl(transferenciaStockRepository,
                transferenciaStockMapper, depositoRepository, empleadoRepository, productoRepository,
                stockDepositoService, transferenciaDetalleMapper, transferenciaStockSpecification);

        central = new Deposito();
        central.setId(1L);
        sucursal = new Deposito();
        sucursal.setId(2L);
        leche = new Producto();
        leche.setId(10L);
        Empleado empleado = new Empleado();
        empleado.setId(5L);
        empleado.setEstado(true);

        lenient().when(depositoRepository.findById(1L)).thenReturn(Optional.of(central));
        lenient().when(depositoRepository.findById(2L)).thenReturn(Optional.of(sucursal));
        lenient().when(empleadoRepository.findById(5L)).thenReturn(Optional.of(empleado));
        lenient().when(productoRepository.findById(10L)).thenReturn(Optional.of(leche));
        lenient().when(ajusteStockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transferenciaStockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(ajusteStockMapper.toEntity(any())).thenAnswer(invocation -> new AjusteStock());
        lenient().when(ajusteDetalleMapper.toEntity(any())).thenAnswer(invocation -> new AjusteStockDetalle());
        lenient().when(transferenciaStockMapper.toEntity(any())).thenAnswer(invocation -> new TransferenciaStock());
        lenient().when(transferenciaDetalleMapper.toEntity(any()))
                .thenAnswer(invocation -> new TransferenciaStockDetalle());
    }

    @Test
    void ajustePositivoIngresaStock() {
        ajusteService.create(ajuste(TipoAjuste.POSITIVO, 4));

        verify(stockDepositoService).aplicarMovimiento(central, leche, 4);
        verify(ajusteStockRepository).save(any(AjusteStock.class));
    }

    @Test
    void ajusteNegativoDescuentaStock() {
        ajusteService.create(ajuste(TipoAjuste.NEGATIVO, 4));

        verify(stockDepositoService).aplicarMovimiento(central, leche, -4);
    }

    @Test
    void ajusteNegativoSinStockSigueRespondiendoStockInsuficiente() {
        doThrow(new InsufficientStockException("Stock insuficiente"))
                .when(stockDepositoService).aplicarMovimiento(central, leche, -4);

        assertThrows(InsufficientStockException.class, () -> ajusteService.create(ajuste(TipoAjuste.NEGATIVO, 4)));
        verify(ajusteStockRepository, never()).save(any());
    }

    @Test
    void transferenciaDescuentaDelOrigenYLuegoAcreditaEnElDestino() {
        transferenciaService.create(transferencia(3));

        InOrder orden = inOrder(stockDepositoService);
        orden.verify(stockDepositoService).aplicarMovimiento(central, leche, -3);
        orden.verify(stockDepositoService).aplicarMovimiento(sucursal, leche, 3);
        verify(transferenciaStockRepository).save(any(TransferenciaStock.class));
    }

    @Test
    void transferenciaSinStockEnOrigenSigueRespondiendoSolicitudInvalida() {
        doThrow(new InsufficientStockException("Stock insuficiente"))
                .when(stockDepositoService).aplicarMovimiento(central, leche, -3);

        assertThrows(BadRequestException.class, () -> transferenciaService.create(transferencia(3)));
        verify(stockDepositoService, never()).aplicarMovimiento(sucursal, leche, 3);
        verify(transferenciaStockRepository, never()).save(any());
    }

    private static AjusteStockRequestDTO ajuste(TipoAjuste tipo, int cantidad) {
        AjusteStockDetalleRequestDTO detalle = new AjusteStockDetalleRequestDTO();
        detalle.setIdProducto(10L);
        detalle.setCantidad(cantidad);
        AjusteStockRequestDTO dto = new AjusteStockRequestDTO();
        dto.setIdDeposito(1L);
        dto.setIdEmpleado(5L);
        dto.setMotivo("Prueba");
        dto.setTipoAjuste(tipo);
        dto.setDetalles(new ArrayList<>(List.of(detalle)));
        return dto;
    }

    private static TransferenciaStockRequestDTO transferencia(int cantidad) {
        TransferenciaStockDetalleRequestDTO detalle = new TransferenciaStockDetalleRequestDTO();
        detalle.setIdProducto(10L);
        detalle.setCantidad(cantidad);
        TransferenciaStockRequestDTO dto = new TransferenciaStockRequestDTO();
        dto.setIdDepositoOrigen(1L);
        dto.setIdDepositoDestino(2L);
        dto.setIdEmpleado(5L);
        dto.setDetalles(new ArrayList<>(List.of(detalle)));
        return dto;
    }
}
