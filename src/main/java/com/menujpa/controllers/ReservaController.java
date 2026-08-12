package com.menujpa.controllers;

import com.menujpa.dto.ReservaRequest;
import com.menujpa.services.ReservaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Map;
import java.util.stream.Collectors;

// Igual criterio que Pedido/Pago: solo lectura + las acciones (reservar/cancelar), nada de
// CRUD generico que esquive la validacion de capacidad/solapamiento de horarios.
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/reservas")
@Tag(name = "Reservas", description = "Reserva de mesas por parte de los clientes")
public class ReservaController {

    @Autowired
    private ReservaServiceImpl reservaService;

    @Operation(summary = "Listar todas las reservas")
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(reservaService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener una reserva por id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reservaService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Reservar una mesa (cliente autenticado)")
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarMesa(@Valid @RequestBody ReservaRequest datos, BindingResult bindingResult,
                                           Authentication authentication) {
        if (bindingResult.hasErrors()) {
            String errores = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores));
        }
        try {
            var reserva = reservaService.reservarMesa(
                authentication.getName(),
                datos.getMesaId(),
                Date.valueOf(datos.getFecha()),
                datos.getHoraInicio(),
                datos.getHoraFin(),
                datos.getCantidadPersonas());
            return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cancelar una reserva propia (o cualquiera, si sos gerente)")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReservacion(@PathVariable Long id, Authentication authentication) {
        try {
            boolean esGerente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
            reservaService.cancelarReservacion(id, authentication.getName(), esGerente);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
