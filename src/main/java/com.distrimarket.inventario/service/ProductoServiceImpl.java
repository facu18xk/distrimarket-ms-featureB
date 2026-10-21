package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.ProductoService;
import com.distrimarket.inventario.specification.ProductoSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductoServiceImpl extends BaseServiceImpl<Producto, ProductoRequestDTO, ProductoResponseDTO> implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductoSpecification productoSpecification;
    private final CacheManager cacheManager;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper, ProductoSpecification productoSpecification, CacheManager cacheManager) {
        super(productoRepository, productoMapper);
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.productoSpecification = productoSpecification;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productos", key = "'api_producto_' + #id")
    public ProductoResponseDTO findById(Long id) {
        log.debug("Cache miss para producto con clave 'api_producto_{}'. Consultando BD...", id);
        return super.findById(id);
    }

    @Override
    @Transactional
    @CachePut(value = "productos", key = "'api_producto_' + #result.id", condition = "#result != null && #result.id != null")
    public ProductoResponseDTO create(ProductoRequestDTO createDTO) {
        validateCreate(createDTO);
        return super.create(createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "productos", key = "'api_producto_' + #id")
    public ProductoResponseDTO update(Long id, ProductoRequestDTO updateDTO) {
        validateUpdate(id, updateDTO);
        log.debug("Actualizando producto ID {} y desalojando clave 'api_producto_{}' de Redis", id, id);
        return super.update(id, updateDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> findAll(Pageable pageable) {
        Page<ProductoResponseDTO> entityPage = super.findAll(pageable);
        // Cada producto recuperado se guarda individualmente
        cachearProductosPage(entityPage, "general");
        return entityPage;
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> findAllWithSpecifications(Long categoriaId, Long marcaId, Pageable pageable) {
        log.debug("Consultando productos con filtros -> categoriaId: {}, marcaId: {}, página: {}, tamaño: {}",
                categoriaId, marcaId, pageable.getPageNumber(), pageable.getPageSize());
        Specification<Producto> spec = Specification
                .where(productoSpecification.hasCategoriaId(categoriaId))
                .and(productoSpecification.hasMarcaId(marcaId));

        Page<Producto> entityPage = productoRepository.findAll(spec, pageable);
        Page<ProductoResponseDTO> dtoPage = entityPage.map(productoMapper::toDTO);

        // Cada producto recuperado se guarda individualmente
        cachearProductosPage(dtoPage, "con especificaciones");

        return dtoPage;
    }

    @Override
    @Transactional
    @CacheEvict(value = "productos", key = "'api_producto_' + #id")
    public void deleteById(Long id) {
        log.debug("Eliminando producto ID {} y desalojando clave 'api_producto_{}' de Redis", id, id);
        super.deleteById(id);
    }

    private void validateCreate(ProductoRequestDTO dto) {
        log.debug("Validando unicidad para nuevo producto: '{}' (Código de barra: '{}')",
                dto.getNombre(), dto.getCodigoBarra());
        if (productoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre: " + dto.getNombre());
        }
        if (dto.getCodigoBarra() != null && !dto.getCodigoBarra().isBlank()
                && productoRepository.existsByCodigoBarra(dto.getCodigoBarra())) {
            throw new DuplicateResourceException("Ya existe un producto con el código de barra: " + dto.getCodigoBarra());
        }
    }

    private void validateUpdate(Long id, ProductoRequestDTO dto) {
        log.debug("Validando unicidad en actualización de producto ID {}: '{}' (Código de barra: '{}')",
                id, dto.getNombre(), dto.getCodigoBarra());
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otro producto con el nombre: " + dto.getNombre());
        }
        if (dto.getCodigoBarra() != null && !dto.getCodigoBarra().isBlank()
                && productoRepository.existsByCodigoBarraAndIdNot(dto.getCodigoBarra(), id)) {
            throw new DuplicateResourceException("Ya existe otro producto con el código de barra: " + dto.getCodigoBarra());
        }
    }

    private void cachearProductosPage(Page<ProductoResponseDTO> page, String tipoConsulta) {
        Cache cache = cacheManager.getCache("productos");
        if (cache != null) {
            page.forEach(dto -> {
                if (dto.getId() != null) {
                    String customKey = "api_producto_" + dto.getId();
                    cache.put(customKey, dto);
                }
            });
            log.debug("Se almacenaron {} productos en caché bajo 'api_producto_*' desde consulta {}",
                    page.getNumberOfElements(), tipoConsulta);
        }
    }
}