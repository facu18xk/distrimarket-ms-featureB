package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.mapper.BaseMapper;
import com.distrimarket.inventario.repository.BaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdenPaginacionTest {

    private final BaseRepository<Producto> repository = mock(BaseRepository.class);
    private final BaseMapper<Producto, ProductoRequestDTO, ProductoResponseDTO> mapper = mock(BaseMapper.class);

    @Test
    void sinOrdenUsaElOrdenPorDefectoYAgregaDesempatePorId() {
        ServicioDePrueba service = servicio(Set.of("id", "nombre"), Sort.by(Sort.Direction.ASC, "id"));
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(PageRequest.of(0, 10));

        assertThat(ordenEnviado()).containsExactly(Sort.Order.asc("id"));
    }

    @Test
    void conservaElOrdenExplicitoPermitidoYAgregaIdComoDesempate() {
        ServicioDePrueba service = servicio(Set.of("id", "nombre"), Sort.by(Sort.Direction.ASC, "id"));
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(PageRequest.of(1, 25, Sort.by(Sort.Direction.DESC, "nombre")));

        Pageable enviado = pageableEnviado();
        assertThat(enviado.getPageNumber()).isEqualTo(1);
        assertThat(enviado.getPageSize()).isEqualTo(25);
        assertThat(enviado.getSort()).containsExactly(
                Sort.Order.desc("nombre"),
                Sort.Order.desc("id"));
    }

    @Test
    void rechazaPropiedadesFueraDeLaAllowlist() {
        ServicioDePrueba service = servicio(Set.of("id", "nombre"), Sort.by(Sort.Direction.ASC, "id"));

        assertThatThrownBy(() -> service.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "precioCosto"))))
                .isInstanceOf(java.lang.IllegalArgumentException.class)
                .hasMessageContaining("precioCosto");

        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void usaFechaCreacionDescYIdDescComoOrdenPorDefectoDeMovimientos() {
        ServicioDePrueba service = servicio(
                Set.of("id", "fechaCreacion"),
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by(Sort.Direction.DESC, "id")));
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(PageRequest.of(0, 10));

        assertThat(ordenEnviado()).containsExactly(
                Sort.Order.desc("fechaCreacion"),
                Sort.Order.desc("id"));
    }

    private ServicioDePrueba servicio(Collection<String> propiedades, Sort ordenPorDefecto) {
        return new ServicioDePrueba(repository, mapper, propiedades, ordenPorDefecto);
    }

    private Pageable pageableEnviado() {
        org.mockito.ArgumentCaptor<Pageable> captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        return captor.getValue();
    }

    private Sort ordenEnviado() {
        return pageableEnviado().getSort();
    }

    private static final class ServicioDePrueba
            extends BaseServiceImpl<Producto, ProductoRequestDTO, ProductoResponseDTO> {

        private final Collection<String> propiedades;
        private final Sort ordenPorDefecto;

        private ServicioDePrueba(BaseRepository<Producto> repository,
                                 BaseMapper<Producto, ProductoRequestDTO, ProductoResponseDTO> mapper,
                                 Collection<String> propiedades,
                                 Sort ordenPorDefecto) {
            super(repository, mapper);
            this.propiedades = propiedades;
            this.ordenPorDefecto = ordenPorDefecto;
        }

        @Override
        protected Collection<String> propiedadesOrdenPermitidas() {
            return propiedades;
        }

        @Override
        protected Sort ordenPorDefecto() {
            return ordenPorDefecto;
        }
    }
}
