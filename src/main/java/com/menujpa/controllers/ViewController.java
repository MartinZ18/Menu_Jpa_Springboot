package com.menujpa.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/gerentes")
    public String gerentes() { return "gerentes"; }

    @GetMapping("/chefs")
    public String chefs() { return "chefs"; }

    @GetMapping("/alimentos")
    public String alimentos() { return "alimentos"; }

    @GetMapping("/recetas")
    public String recetas() { return "recetas"; }

    @GetMapping("/menus")
    public String menus() { return "menus"; }
}
