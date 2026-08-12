package com.menujpa.controllers;

import com.menujpa.entities.Despensa;
import com.menujpa.services.DespensaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/despensas")
@Tag(name = "Despensas", description = "Stock de ingredientes administrado por el gerente")
public class DespensaController extends BaseControllerImpl<Despensa, DespensaServiceImpl> {

    @Autowired
    private DespensaServiceImpl despensaService;

    @Operation(summary = "Agregar un ingrediente a la despensa")
    @PostMapping("/{despensaId}/ingredientes/{ingredienteId}")
    public ResponseEntity<?> agregarIngrediente(@PathVariable Long despensaId, @PathVariable Long ingredienteId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                .body(despensaService.agregarIngrediente(despensaId, ingredienteId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Quitar un ingrediente de la despensa")
    @DeleteMapping("/{despensaId}/ingredientes/{ingredienteId}")
    public ResponseEntity<?> quitarIngrediente(@PathVariable Long despensaId, @PathVariable Long ingredienteId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                .body(despensaService.quitarIngrediente(despensaId, ingredienteId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
