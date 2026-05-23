package com.menujpa.controllers;

import com.menujpa.entities.Receta;
import com.menujpa.services.RecetaServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/recetas")
public class RecetaController extends BaseControllerImpl<Receta, RecetaServiceImpl> {
}
