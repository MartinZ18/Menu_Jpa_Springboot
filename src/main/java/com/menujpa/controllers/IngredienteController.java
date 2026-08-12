package com.menujpa.controllers;

import com.menujpa.entities.Ingrediente;
import com.menujpa.services.IngredienteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/ingredientes")
@Tag(name = "Ingredientes", description = "Stock de ingredientes con descripción y cantidad")
public class IngredienteController extends BaseControllerImpl<Ingrediente, IngredienteServiceImpl> {
}
