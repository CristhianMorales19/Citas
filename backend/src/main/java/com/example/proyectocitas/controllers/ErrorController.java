package com.example.proyectocitas.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
    
    @GetMapping("/error")
    public String handleError() {
        return "error/error";
    }
}
