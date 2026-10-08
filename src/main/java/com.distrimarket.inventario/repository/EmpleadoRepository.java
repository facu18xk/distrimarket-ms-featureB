package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends BaseRepository<Empleado> {
    boolean existsByPersonaId(Long personaId);
    boolean existsByPersonaIdAndIdNot(Long personaId, Long id);
    @EntityGraph(attributePaths = {"persona"})
    Optional<Empleado> findByPersonaId(Long personaId);

    @Override
    @EntityGraph(attributePaths = {"persona"})
    Optional<Empleado> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"persona"})
    Page<Empleado> findAll(Specification<Empleado> spec, Pageable pageable);
}