package com.menujpa.controllers;

import com.menujpa.entities.Chef;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.services.ChefServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/chefs")
@Tag(name = "Chefs", description = "Personal de cocina: especialidad, experiencia y horarios")
public class ChefController extends BaseControllerImpl<Chef, ChefServiceImpl> {

    @Autowired
    private ChefRepository chefRepository;

    @Operation(summary = "Registrar la entrada del chef autenticado")
    @PostMapping("/fichar/entrada")
    public ResponseEntity<?> registrarEntrada(Authentication authentication) {
        try {
            Chef chef = chefRepository.findByUsuario(authentication.getName())
                .orElseThrow(() -> new Exception("Chef no encontrado para el usuario autenticado."));
            return ResponseEntity.ok(servicio.registrarEntrada(chef.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Registrar la salida del chef autenticado")
    @PostMapping("/fichar/salida")
    public ResponseEntity<?> registrarSalida(Authentication authentication) {
        try {
            Chef chef = chefRepository.findByUsuario(authentication.getName())
                .orElseThrow(() -> new Exception("Chef no encontrado para el usuario autenticado."));
            return ResponseEntity.ok(servicio.registrarSalida(chef.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
