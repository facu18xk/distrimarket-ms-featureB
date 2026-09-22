package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Persona;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends BaseRepository<Persona> {

    // Métodos útiles para evitar duplicados de documentos
    boolean existsByCi(String ci);
    boolean existsByRuc(String ruc);
    boolean existsByCiAndIdNot(String ci, Long id);
    boolean existsByRucAndIdNot(String ruc, Long id);
    Optional findByCi(String ci);
    Optional findByRuc(String ruc);
}