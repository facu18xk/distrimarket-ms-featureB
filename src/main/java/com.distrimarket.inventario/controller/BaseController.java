package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.BaseEntity;
import com.distrimarket.inventario.service.BaseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class BaseController<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO> {

    protected final BaseService<E, CREATE_DTO, RESPONSE_DTO> service;

    public BaseController(BaseService<E, CREATE_DTO, RESPONSE_DTO> service) {
        this.service = service;
    }

    /*@GetMapping
    public ResponseEntity<List<RESPONSE_DTO>> getAll(){
        return ResponseEntity.ok(service.findAll());
    }*/

    /*@GetMapping
    public ResponseEntity<Page<RESPONSE_DTO>> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }*/

    // Método reutilizable sin @GetMapping directo para no colisionar con firmas hijas
    protected ResponseEntity<Page<RESPONSE_DTO>> doGetAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RESPONSE_DTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<RESPONSE_DTO> create(@Valid @RequestBody CREATE_DTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(createDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RESPONSE_DTO> update(@PathVariable Long id, @Valid @RequestBody CREATE_DTO createDTO) {
        return ResponseEntity.ok(service.update(id, createDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}