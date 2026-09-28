package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Persona;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends BaseRepository<Persona> {
    boolean existsByCi(String ci);
    boolean existsByRuc(String ruc);
    boolean existsByCorreo(String correo);
    Optional<Persona> findByCi(String ci);
    Optional<Persona> findByRuc(String ruc);
}
