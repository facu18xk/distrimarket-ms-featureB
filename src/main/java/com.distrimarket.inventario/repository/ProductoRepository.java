package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends BaseRepository<Producto> {
    boolean existsByCodigoBarra(String codigoBarra);
}