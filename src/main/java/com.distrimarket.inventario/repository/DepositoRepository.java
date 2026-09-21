package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Deposito;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositoRepository extends BaseRepository<Deposito> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Deposito> findByNombreIgnoreCase(String nombre);
}