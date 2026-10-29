package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.mapper.DepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.repository.StockDepositoRepository;
import com.distrimarket.inventario.specification.DepositoSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepositoServiceImplTest {

    private final DepositoRepository depositos = mock(DepositoRepository.class);
    private final StockDepositoRepository existencias = mock(StockDepositoRepository.class);
    private final DepositoServiceImpl service = new DepositoServiceImpl(depositos, mock(DepositoMapper.class),
            mock(CacheManager.class), mock(DepositoSpecification.class), existencias);

    @Test
    void noSeDaDeBajaUnDepositoConExistencias() {
        when(existencias.countByDepositoId(7L)).thenReturn(3L);

        assertThatThrownBy(() -> service.deleteById(7L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("3 existencias");
        verify(depositos, never()).save(any());
    }

    @Test
    void sinExistenciasLaBajaEsLogica() {
        Deposito d = new Deposito();
        d.setId(7L);
        d.setActivo(true);
        when(existencias.countByDepositoId(7L)).thenReturn(0L);
        when(depositos.findById(7L)).thenReturn(Optional.of(d));

        service.deleteById(7L);

        assertThat(d.getActivo()).isFalse();
    }
}
