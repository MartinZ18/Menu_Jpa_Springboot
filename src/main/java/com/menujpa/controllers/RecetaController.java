package com.menujpa.controllers;

import com.menujpa.entities.Receta;
import com.menujpa.services.RecetaServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/recetas")
@Tag(name = "Recetas", description = "Recetas con dificultad, tiempo de preparación y alimentos asociados")
public class RecetaController extends BaseControllerImpl<Receta, RecetaServiceImpl> {
}
