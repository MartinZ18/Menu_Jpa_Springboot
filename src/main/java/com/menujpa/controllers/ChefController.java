package com.menujpa.controllers;

import com.menujpa.entities.Chef;
import com.menujpa.services.ChefServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/chefs")
public class ChefController extends BaseControllerImpl<Chef, ChefServiceImpl> {
}
