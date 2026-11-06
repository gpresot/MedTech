package com.example.MedTech.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.MedTech.model.Profissional;
import com.example.MedTech.service.ProfissionalService;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @GetMapping
    public List<Profissional> listarTodos() {
        return profissionalService.listarTodos();
    }
}
