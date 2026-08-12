package com.menujpa.controllers;

import com.menujpa.entities.Menu;
import com.menujpa.services.MenuServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/menus")
@Tag(name = "Menús", description = "Gestión de menús y sus recetas asociadas")
public class MenuController extends BaseControllerImpl<Menu, MenuServiceImpl> {

    @Autowired
    private MenuServiceImpl menuService;

    @Operation(summary = "Agregar una receta a un menú")
    @PostMapping("/{menuId}/recetas/{recetaId}")
    public ResponseEntity<?> agregarReceta(@PathVariable Long menuId, @PathVariable Long recetaId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                .body(menuService.agregarReceta(menuId, recetaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Quitar una receta de un menú")
    @DeleteMapping("/{menuId}/recetas/{recetaId}")
    public ResponseEntity<?> quitarReceta(@PathVariable Long menuId, @PathVariable Long recetaId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                .body(menuService.quitarReceta(menuId, recetaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
