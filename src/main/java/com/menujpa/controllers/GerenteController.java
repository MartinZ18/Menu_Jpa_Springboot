package com.menujpa.controllers;

import com.menujpa.entities.Gerente;
import com.menujpa.services.GerenteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/gerentes")
@Tag(name = "Gerentes", description = "Responsables de menús y despensa")
public class GerenteController extends BaseControllerImpl<Gerente, GerenteServiceImpl> {
}
