package com.menujpa.controllers;

import com.menujpa.entities.Alimento;
import com.menujpa.services.AlimentoServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/alimentos")
@Tag(name = "Alimentos", description = "Catálogo de alimentos: platos fuertes, bebidas, postres y adicionales")
public class AlimentoController extends BaseControllerImpl<Alimento, AlimentoServiceImpl> {
}
