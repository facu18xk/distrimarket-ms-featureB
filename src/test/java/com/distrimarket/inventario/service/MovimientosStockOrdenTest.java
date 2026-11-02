package com.distrimarket.inventario.service;

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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovimientosStockOrdenTest {

    private final AjusteStockRepository ajusteRepository = mock(AjusteStockRepository.class);
    private final AjusteStockServiceImpl ajustes = new AjusteStockServiceImpl(
            ajusteRepository,
            mock(AjusteStockMapper.class),
            mock(DepositoRepository.class),
            mock(EmpleadoRepository.class),
            mock(ProductoRepository.class),
            mock(StockDepositoService.class),
            mock(AjusteStockDetalleMapper.class),
            new AjusteStockSpecification());

    private final TransferenciaStockRepository transferenciaRepository = mock(TransferenciaStockRepository.class);
    private final TransferenciaStockServiceImpl transferencias = new TransferenciaStockServiceImpl(
            transferenciaRepository,
            mock(TransferenciaStockMapper.class),
            mock(DepositoRepository.class),
            mock(EmpleadoRepository.class),
            mock(ProductoRepository.class),
            mock(StockDepositoService.class),
            mock(TransferenciaStockDetalleMapper.class),
            new TransferenciaStockSpecification());

    @Test
    void ajustesSinOrdenUsanFechaCreacionDescEIdDesc() {
        when(ajusteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        ajustes.findAllWithSpecifications(null, null, null, PageRequest.of(0, 10));

        assertThat(ordenDe(ajusteRepository)).containsExactly(
                Sort.Order.desc("fechaCreacion"),
                Sort.Order.desc("id"));
    }

    @Test
    void transferenciasSinOrdenUsanFechaCreacionDescEIdDesc() {
        when(transferenciaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        transferencias.findAllWithSpecifications(null, null, null, null, PageRequest.of(0, 10));

        assertThat(ordenDe(transferenciaRepository)).containsExactly(
                Sort.Order.desc("fechaCreacion"),
                Sort.Order.desc("id"));
    }

    @Test
    void unOrdenExplicitoDelFrontendSigueFuncionando() {
        when(ajusteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        ajustes.findAllWithSpecifications(null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "fechaCreacion")));

        assertThat(ordenDe(ajusteRepository)).containsExactly(
                Sort.Order.asc("fechaCreacion"),
                Sort.Order.asc("id"));
    }

    private static Sort ordenDe(AjusteStockRepository repository) {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        return captor.getValue().getSort();
    }

    private static Sort ordenDe(TransferenciaStockRepository repository) {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        return captor.getValue().getSort();
    }
}
