package com.menujpa.controllers;

import com.menujpa.entities.Alimento;
import com.menujpa.services.AlimentoServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/alimentos")
public class AlimentoController extends BaseControllerImpl<Alimento, AlimentoServiceImpl> {
}
