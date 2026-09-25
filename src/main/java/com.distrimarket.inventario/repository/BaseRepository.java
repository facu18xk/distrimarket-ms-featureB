package com.distrimarket.inventario.repository;

import com.distrimarket.commons.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {
    //E save(E entity);
    //Optional<E> findById(Long id);
    //List<E> findAll();
    //void deleteById(Long id);
    //boolean existsById(Long id);
    //OBS: estos métodos ya vienen integrados con JpaRepository
}
