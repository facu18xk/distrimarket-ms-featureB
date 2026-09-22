package com.distrimarket.inventario.repository;

import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.stereotype.Repository;

@Repository
public interface AjusteStockRepository extends BaseRepository<AjusteStock> {
}