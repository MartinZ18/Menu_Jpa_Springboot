package com.menujpa.controllers;

import com.menujpa.entities.Menu;
import com.menujpa.services.MenuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/menus")
public class MenuController extends BaseControllerImpl<Menu, MenuServiceImpl> {

    @Autowired
    private MenuServiceImpl menuService;

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
