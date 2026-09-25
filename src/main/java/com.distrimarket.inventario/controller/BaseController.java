package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.BaseEntity;
import com.distrimarket.inventario.service.BaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public abstract class BaseController<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO> {

    protected final BaseService<E, CREATE_DTO, RESPONSE_DTO> service;

    public BaseController(BaseService<E, CREATE_DTO, RESPONSE_DTO> service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RESPONSE_DTO>> getAll(){
        return ResponseEntity.ok(service.findAll());
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
    public ResponseEntity<RESPONSE_DTO> update(@Valid @PathVariable Long id, @RequestBody CREATE_DTO createDTO) {
        return ResponseEntity.ok(service.update(id, createDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}