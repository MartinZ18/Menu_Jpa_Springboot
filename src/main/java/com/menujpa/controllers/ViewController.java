package com.menujpa.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @GetMapping("/gerentes")
    public String gerentes() { return "gerentes"; }

    @GetMapping("/chefs")
    public String chefs() { return "chefs"; }

    @GetMapping("/meseros")
    public String meseros() { return "meseros"; }

    @GetMapping("/alimentos")
    public String alimentos() { return "alimentos"; }

    @GetMapping("/recetas")
    public String recetas() { return "recetas"; }

    @GetMapping("/menus")
    public String menus() { return "menus"; }

    @GetMapping("/clientes")
    public String clientes() { return "clientes"; }

    @GetMapping("/ingredientes")
    public String ingredientes() { return "ingredientes"; }

    @GetMapping("/despensas")
    public String despensas() { return "despensas"; }

    @GetMapping("/mesas")
    public String mesas() { return "mesas"; }

    @GetMapping("/pedidos")
    public String pedidos() { return "pedidos"; }

    @GetMapping("/pagos")
    public String pagos() { return "pagos"; }

    @GetMapping("/reservas")
    public String reservas() { return "reservas"; }
}
