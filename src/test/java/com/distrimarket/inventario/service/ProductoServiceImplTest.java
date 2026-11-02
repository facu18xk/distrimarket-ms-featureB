package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.specification.ProductoSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.CacheManager;
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

class ProductoServiceImplTest {

    private final ProductoRepository repository = mock(ProductoRepository.class);
    private final ProductoServiceImpl service = new ProductoServiceImpl(
            repository, mock(ProductoMapper.class), new ProductoSpecification(), mock(CacheManager.class));

    @Test
    void aplicaElFiltroEstadoYNormalizaElOrden() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.findAllWithSpecifications(null, null, null, true, PageRequest.of(0, 10));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<Producto>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(specCaptor.capture(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort()).containsExactly(Sort.Order.asc("id"));

        Root<Producto> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> estado = mock(Path.class);
        when(root.get("estado")).thenReturn(estado);
        when(cb.conjunction()).thenReturn(mock(Predicate.class));
        when(cb.equal(estado, Boolean.TRUE)).thenReturn(mock(Predicate.class));

        specCaptor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(estado, Boolean.TRUE);
    }
}
