package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Empleado;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends BaseRepository<Empleado> {
    boolean existsByPersonaId(Long personaId);
    boolean existsByPersonaIdAndIdNot(Long personaId, Long id);
    Optional<Empleado> findByPersonaId(Long personaId);
}