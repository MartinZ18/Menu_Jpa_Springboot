package com.menujpa.controllers;

import com.menujpa.entities.Mesero;
import com.menujpa.repositories.MeseroRepository;
import com.menujpa.services.MeseroServiceImpl;
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
@RequestMapping(path = "api/v1/meseros")
@Tag(name = "Meseros", description = "Personal de sala: toma y entrega los pedidos")
public class MeseroController extends BaseControllerImpl<Mesero, MeseroServiceImpl> {

    @Autowired
    private MeseroRepository meseroRepository;

    @Operation(summary = "Registrar la entrada del mesero autenticado")
    @PostMapping("/fichar/entrada")
    public ResponseEntity<?> registrarEntrada(Authentication authentication) {
        try {
            Mesero mesero = meseroRepository.findByUsuario(authentication.getName())
                .orElseThrow(() -> new Exception("Mesero no encontrado para el usuario autenticado."));
            return ResponseEntity.ok(servicio.registrarEntrada(mesero.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Registrar la salida del mesero autenticado")
    @PostMapping("/fichar/salida")
    public ResponseEntity<?> registrarSalida(Authentication authentication) {
        try {
            Mesero mesero = meseroRepository.findByUsuario(authentication.getName())
                .orElseThrow(() -> new Exception("Mesero no encontrado para el usuario autenticado."));
            return ResponseEntity.ok(servicio.registrarSalida(mesero.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
