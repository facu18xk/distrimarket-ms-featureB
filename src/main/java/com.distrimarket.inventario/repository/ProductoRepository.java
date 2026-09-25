package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Producto;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends BaseRepository<Producto> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCodigoBarra(String codigoBarra);
}
