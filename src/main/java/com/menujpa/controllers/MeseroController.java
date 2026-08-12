package com.menujpa.controllers;

import com.menujpa.entities.Mesero;
import com.menujpa.services.MeseroServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/meseros")
@Tag(name = "Meseros", description = "Personal de sala: toma y entrega los pedidos")
public class MeseroController extends BaseControllerImpl<Mesero, MeseroServiceImpl> {
}
