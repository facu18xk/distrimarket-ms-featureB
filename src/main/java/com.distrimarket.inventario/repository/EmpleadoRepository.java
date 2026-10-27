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

    /**
     * @EntityGraph precarga la relación @ManyToOne LAZY "persona" en un solo SELECT con LEFT JOIN.
     */
    @EntityGraph(attributePaths = {"persona"})
    Optional<Empleado> findByPersonaId(Long personaId);

    /**
     * @EntityGraph precarga los datos de "persona" al buscar un empleado por ID,
     * evitando una segunda consulta SQL al mapear EmpleadoResponseDTO.
     */
    @Override
    @EntityGraph(attributePaths = {"persona"})
    Optional<Empleado> findById(Long id);

    /**
     * @EntityGraph precarga "persona" para todos los empleados de la página sin filtros,
     * evitando N consultas LAZY adicionales a la tabla personas.
     */
    @Override
    @EntityGraph(attributePaths = {"persona"})
    Page<Empleado> findAll(Pageable pageable);

    /**
     * @EntityGraph precarga "persona" en consultas paginadas con filtros (Specification),
     * evitando el problema N+1 en el listado de empleados.
     */
    @Override
    @EntityGraph(attributePaths = {"persona"})
    Page<Empleado> findAll(Specification<Empleado> spec, Pageable pageable);
}