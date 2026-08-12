package com.menujpa.controllers;

import com.menujpa.entities.Cliente;
import com.menujpa.services.ClienteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/clientes")
@Tag(name = "Clientes", description = "Registro de clientes con usuario y contraseña")
public class ClienteController extends BaseControllerImpl<Cliente, ClienteServiceImpl> {
}
