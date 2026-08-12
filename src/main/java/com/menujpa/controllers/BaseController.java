package com.menujpa.controllers;

import com.menujpa.entities.Base;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.io.Serializable;

public interface BaseController<E extends Base, ID extends Serializable> {

    @Operation(summary = "Listar todos los registros")
    ResponseEntity<?> getAll();

    @Operation(summary = "Listar registros paginados")
    ResponseEntity<?> getAll(Pageable pageable);

    @Operation(summary = "Obtener un registro por id")
    ResponseEntity<?> getOne(@Parameter(description = "Id del registro") @PathVariable ID id);

    @Operation(summary = "Crear un nuevo registro")
    ResponseEntity<?> save(@Valid @RequestBody E entity, BindingResult bindingResult);

    @Operation(summary = "Actualizar un registro existente")
    ResponseEntity<?> update(@Parameter(description = "Id del registro") @PathVariable ID id, @Valid @RequestBody E entity, BindingResult bindingResult);

    @Operation(summary = "Eliminar un registro por id")
    ResponseEntity<?> delete(@Parameter(description = "Id del registro") @PathVariable ID id);
}
