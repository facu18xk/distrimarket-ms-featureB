package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.config.CacheConfig;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.ProductoService;
import com.distrimarket.inventario.specification.ProductoSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductoServiceImpl extends BaseServiceImpl<Producto, ProductoRequestDTO, ProductoResponseDTO> implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductoSpecification productoSpecification;
    private final CacheManager cacheManager;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProductoMapper productoMapper,
                               ProductoSpecification productoSpecification,
                               CacheManager cacheManager) {
        super(productoRepository, productoMapper);
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.productoSpecification = productoSpecification;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PRODUCTO, key = "#id")
    public ProductoResponseDTO findById(Long id) {
        log.debug("Cache miss para producto ID {}. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.PRODUCTO, key = "#result.id", condition = "#result != null && #result.id != null")
    public ProductoResponseDTO create(ProductoRequestDTO createDTO) {
        validateCreate(createDTO);
        return super.create(createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCTO, key = "#id")
    public ProductoResponseDTO update(Long id, ProductoRequestDTO updateDTO) {
        validateUpdate(id, updateDTO);
        log.debug("Actualizando producto ID {} e invalidando clave de Redis", id);
        return super.update(id, updateDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PRODUCTO, key = "#id")
    public void deleteById(Long id) {
        log.debug("Eliminando producto ID {} y desalojando clave de Redis", id);
        super.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> findAll(Pageable pageable) {
        Page<ProductoResponseDTO> entityPage = super.findAll(pageable);
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

        cachearProductosPage(dtoPage, "con especificaciones");

        return dtoPage;
    }

    private void cachearProductosPage(Page<ProductoResponseDTO> page, String tipoConsulta) {
        Cache cache = cacheManager.getCache(CacheConfig.PRODUCTO);
        if (cache != null) {
            page.forEach(dto -> {
                if (dto.getId() != null) {
                    // Se usa directamente el Long id como clave
                    cache.put(dto.getId(), dto);
                }
            });
            log.debug("Se almacenaron {} productos individualmente en caché desde consulta {}",
                    page.getNumberOfElements(), tipoConsulta);
        }
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
}