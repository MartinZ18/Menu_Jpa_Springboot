package com.menujpa.controllers;

import com.menujpa.entities.Base;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.io.Serializable;

public interface BaseController<E extends Base, ID extends Serializable> {
    ResponseEntity<?> getAll();
    ResponseEntity<?> getAll(Pageable pageable);
    ResponseEntity<?> getOne(@PathVariable ID id);
    ResponseEntity<?> save(@Valid @RequestBody E entity, BindingResult bindingResult);
    ResponseEntity<?> update(@PathVariable ID id, @Valid @RequestBody E entity, BindingResult bindingResult);
    ResponseEntity<?> delete(@PathVariable ID id);
}
