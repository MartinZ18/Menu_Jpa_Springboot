package com.menujpa.controllers;

import com.menujpa.entities.Mesa;
import com.menujpa.services.MesaServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/mesas")
@Tag(name = "Mesas", description = "Mesas del salón: número y capacidad")
public class MesaController extends BaseControllerImpl<Mesa, MesaServiceImpl> {
}
