package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.dto.StockMovimientoDetalleRequestDTO;
import com.distrimarket.commons.dto.StockMovimientoRequestDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.exception.IllegalArgumentException;
import com.distrimarket.inventario.exception.InsufficientStockException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.StockDepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.repository.StockDepositoRepository;
import com.distrimarket.inventario.specification.StockDepositoSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockDepositoServiceImplTest {

    @Mock private StockDepositoRepository stockDepositoRepository;
    @Mock private StockDepositoMapper stockDepositoMapper;
    @Mock private DepositoRepository depositoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private StockDepositoSpecification stockDepositoSpecification;

    private StockDepositoServiceImpl service;
    private Deposito deposito;
    private Producto leche;
    private Producto pan;

    @BeforeEach
    void setUp() {
        service = new StockDepositoServiceImpl(stockDepositoRepository, stockDepositoMapper,
                depositoRepository, productoRepository, stockDepositoSpecification);
        deposito = new Deposito();
        deposito.setId(1L);
        leche = producto(10L, "Leche");
        pan = producto(20L, "Pan");
        lenient().when(stockDepositoRepository.save(any(StockDeposito.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void cantidadPositivaSumaAlStockExistente() {
        conStock(leche, 5);

        StockDeposito resultado = service.aplicarMovimiento(deposito, leche, 3);

        assertEquals(8, resultado.getCantidad());
        verify(stockDepositoRepository).save(resultado);
    }

    @Test
    void cantidadNegativaDescuentaDelStock() {
        conStock(leche, 5);

        StockDeposito resultado = service.aplicarMovimiento(deposito, leche, -5);

        assertEquals(0, resultado.getCantidad());
    }

    @Test
    void ingresoCreaElRegistroSiElProductoNoTeniaStockEnElDeposito() {
        sinStock(leche);

        StockDeposito resultado = service.aplicarMovimiento(deposito, leche, 4);

        assertEquals(4, resultado.getCantidad());
        assertSame(deposito, resultado.getDeposito());
        assertSame(leche, resultado.getProducto());
    }

    @Test
    void descontarMasDeLoDisponibleSeRechazaYNoGuarda() {
        conStock(leche, 2);

        InsufficientStockException error = assertThrows(InsufficientStockException.class,
                () -> service.aplicarMovimiento(deposito, leche, -3));

        assertTrue(error.getMessage().contains("Disponible: 2"));
        assertTrue(error.getMessage().contains("Solicitado descontar: 3"));
        verify(stockDepositoRepository, never()).save(any());
    }

    @Test
    void descontarSinRegistroDeStockSeRechaza() {
        sinStock(leche);

        assertThrows(InsufficientStockException.class, () -> service.aplicarMovimiento(deposito, leche, -1));
        verify(stockDepositoRepository, never()).save(any());
    }

    @Test
    void cantidadCeroSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> service.aplicarMovimiento(deposito, leche, 0));
    }

    @Test
    void superarElMaximoDeStockSeRechaza() {
        conStock(leche, Integer.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, () -> service.aplicarMovimiento(deposito, leche, 1));
        verify(stockDepositoRepository, never()).save(any());
    }

    @Test
    void movimientosAgrupanPorProductoYBloqueanEnOrdenDeId() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(leche));
        when(productoRepository.findById(20L)).thenReturn(Optional.of(pan));
        StockDeposito stockLeche = conStock(leche, 10);
        StockDeposito stockPan = conStock(pan, 10);
        when(stockDepositoMapper.toDTO(any(StockDeposito.class))).thenReturn(new StockDepositoResponseDTO());

        // El pan llega primero y la leche repetida: -2 y -1 deben aplicarse como un único -3.
        List<StockDepositoResponseDTO> resultado = service.aplicarMovimientos(
                movimiento(1L, detalle(20L, 4), detalle(10L, -2), detalle(10L, -1)));

        assertEquals(2, resultado.size());
        assertEquals(7, stockLeche.getCantidad());
        assertEquals(14, stockPan.getCantidad());
        InOrder orden = inOrder(stockDepositoRepository);
        orden.verify(stockDepositoRepository).findWithLockByDepositoIdAndProductoId(1L, 10L);
        orden.verify(stockDepositoRepository).findWithLockByDepositoIdAndProductoId(1L, 20L);
    }

    @Test
    void movimientoConProductoSinStockSuficienteFallaCompleto() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(leche));
        when(productoRepository.findById(20L)).thenReturn(Optional.of(pan));
        conStock(leche, 10);
        conStock(pan, 1);
        when(stockDepositoMapper.toDTO(any(StockDeposito.class))).thenReturn(new StockDepositoResponseDTO());

        // La excepción revierte la transacción, por lo que el descuento previo de leche no persiste.
        assertThrows(InsufficientStockException.class, () -> service.aplicarMovimientos(
                movimiento(1L, detalle(10L, -2), detalle(20L, -5))));
    }

    @Test
    void movimientoEnDepositoInexistenteDaNoEncontrado() {
        when(depositoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.aplicarMovimientos(movimiento(99L, detalle(10L, 1))));
        verify(stockDepositoRepository, never()).save(any());
    }

    @Test
    void movimientoConProductoInexistenteDaNoEncontrado() {
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        when(productoRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.aplicarMovimientos(movimiento(1L, detalle(77L, 1))));
        verify(stockDepositoRepository, never()).save(any());
    }

    @Test
    void movimientoSinDetallesOConCantidadCeroSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> service.aplicarMovimientos(movimiento(1L)));

        when(depositoRepository.findById(1L)).thenReturn(Optional.of(deposito));
        assertThrows(IllegalArgumentException.class,
                () -> service.aplicarMovimientos(movimiento(1L, detalle(10L, 0))));
        verify(stockDepositoRepository, never()).save(any());
    }

    private StockDeposito conStock(Producto producto, int cantidad) {
        StockDeposito stock = StockDeposito.builder().deposito(deposito).producto(producto).cantidad(cantidad).build();
        lenient().when(stockDepositoRepository.findWithLockByDepositoIdAndProductoId(deposito.getId(), producto.getId()))
                .thenReturn(Optional.of(stock));
        return stock;
    }

    private void sinStock(Producto producto) {
        when(stockDepositoRepository.findWithLockByDepositoIdAndProductoId(deposito.getId(), producto.getId()))
                .thenReturn(Optional.empty());
    }

    private static Producto producto(Long id, String nombre) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        return producto;
    }

    private static StockMovimientoRequestDTO movimiento(Long idDeposito, StockMovimientoDetalleRequestDTO... detalles) {
        StockMovimientoRequestDTO dto = new StockMovimientoRequestDTO();
        dto.setIdDeposito(idDeposito);
        dto.setMotivo("Prueba");
        dto.setDetalles(new ArrayList<>(List.of(detalles)));
        return dto;
    }

    private static StockMovimientoDetalleRequestDTO detalle(Long idProducto, int cantidad) {
        StockMovimientoDetalleRequestDTO detalle = new StockMovimientoDetalleRequestDTO();
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);
        return detalle;
    }
}
