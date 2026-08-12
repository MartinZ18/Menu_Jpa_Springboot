package com.menujpa.controllers;

import com.menujpa.entities.Pedido;
import com.menujpa.services.PedidoServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/pedidos")
@Tag(name = "Pedidos", description = "Pedidos realizados por los clientes")
public class PedidoController extends BaseControllerImpl<Pedido, PedidoServiceImpl> {
}
