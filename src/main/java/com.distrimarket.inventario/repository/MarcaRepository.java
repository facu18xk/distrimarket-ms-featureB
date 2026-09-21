package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Marca;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MarcaRepository extends BaseRepository<Marca> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Marca> findByNombreIgnoreCase(String nombre);
}

