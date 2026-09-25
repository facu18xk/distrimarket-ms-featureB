package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Categoria;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends BaseRepository<Categoria> {
    boolean existsByNombreIgnoreCase(String nombre);
}
