package com.menujpa.controllers;

import com.menujpa.entities.Chef;
import com.menujpa.services.ChefServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/chefs")
@Tag(name = "Chefs", description = "Personal de cocina: especialidad, experiencia y horarios")
public class ChefController extends BaseControllerImpl<Chef, ChefServiceImpl> {
}
