package com.menujpa.controllers;

import com.menujpa.dto.GenerarPagoRequest;
import com.menujpa.services.PagoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

// Solo lectura + la accion de generar: un pago es un registro contable, no tiene sentido
// dejarlo editable/borrable por un PUT/DELETE generico.
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/pagos")
@Tag(name = "Pagos", description = "Pagos de nomina generados para Chefs y Meseros")
public class PagoController {

    @Autowired
    private PagoServiceImpl pagoService;

    @Operation(summary = "Listar todos los pagos")
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(pagoService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener un pago por id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pagoService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Generar el pago de un Chef o Mesero (monto = su salario)")
    @PostMapping("/generar")
    public ResponseEntity<?> generarPago(@Valid @RequestBody GenerarPagoRequest datos, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errores = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores));
        }
        try {
            var pago = pagoService.generarPago(datos.getEmpleadoId(), datos.getRol());
            return ResponseEntity.status(HttpStatus.CREATED).body(pago);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
