package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.Deposito;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepository extends BaseRepository<Deposito> {
    boolean existsByNombreIgnoreCase(String nombre);
}
