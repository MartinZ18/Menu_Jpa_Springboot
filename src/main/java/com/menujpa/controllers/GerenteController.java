package com.menujpa.controllers;

import com.menujpa.entities.Gerente;
import com.menujpa.services.GerenteServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/gerentes")
public class GerenteController extends BaseControllerImpl<Gerente, GerenteServiceImpl> {
}
