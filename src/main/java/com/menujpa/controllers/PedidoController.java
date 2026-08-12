package com.menujpa.controllers;

import com.menujpa.dto.PedidoRequest;
import com.menujpa.services.PedidoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

// No extiende BaseControllerImpl a proposito: el precio y el estado de un pedido se calculan
// con reglas de negocio (ver PedidoServiceImpl), asi que un POST/PUT generico las esquivaria.
// Solo se exponen lectura + las acciones de ciclo de vida.
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/pedidos")
@Tag(name = "Pedidos", description = "Ciclo de vida de los pedidos: tomar, modificar, entregar y cancelar")
public class PedidoController {

    @Autowired
    private PedidoServiceImpl pedidoService;

    @Operation(summary = "Listar todos los pedidos")
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(pedidoService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener un pedido por id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Tomar un pedido nuevo (mesero autenticado)")
    @PostMapping("/tomar")
    public ResponseEntity<?> tomarPedido(@Valid @RequestBody PedidoRequest datos, BindingResult bindingResult,
                                          Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores(bindingResult)));
        }
        try {
            var pedido = pedidoService.tomarPedido(authentication.getName(), datos.getClienteIds(), datos.getAlimentoIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Modificar los alimentos de un pedido no entregado (solo el mesero que lo tomó, o un gerente)")
    @PutMapping("/{id}/modificar")
    public ResponseEntity<?> modificarPedido(@PathVariable Long id, @Valid @RequestBody PedidoRequest datos,
                                              BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores(bindingResult)));
        }
        try {
            var pedido = pedidoService.modificarPedido(id, datos.getClienteIds(), datos.getAlimentoIds(),
                authentication.getName(), esGerente(authentication));
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Marcar un pedido como entregado (solo el mesero que lo tomó, o un gerente)")
    @PostMapping("/{id}/entregar")
    public ResponseEntity<?> entregarPedido(@PathVariable Long id, Authentication authentication) {
        try {
            var pedido = pedidoService.entregarPedido(id, authentication.getName(), esGerente(authentication));
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cancelar un pedido no entregado (solo el mesero que lo tomó, o un gerente)")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id, Authentication authentication) {
        try {
            pedidoService.cancelarPedido(id, authentication.getName(), esGerente(authentication));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private boolean esGerente(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
    }

    private String errores(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
    }
}
