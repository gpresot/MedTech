package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.QuartoRequest;
import com.example.MedTech.dto.QuartoResponse;
import com.example.MedTech.dto.QuartoStatusRequest;
import com.example.MedTech.service.QuartoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService quartoService;

    public QuartoController(QuartoService quartoService) {
        this.quartoService = quartoService;
    }

    @GetMapping
    public List<QuartoResponse> listar() {
        return quartoService.listar();
    }

    @GetMapping("/disponiveis")
    public List<QuartoResponse> listarDisponiveis() {
        return quartoService.listarDisponiveis();
    }

    @GetMapping("/{id}")
    public QuartoResponse buscar(@PathVariable Long id) {
        return quartoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<QuartoResponse> cadastrar(@Valid @RequestBody QuartoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quartoService.cadastrar(req));
    }

    @PutMapping("/{id}")
    public QuartoResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody QuartoRequest req) {
        return quartoService.atualizar(id, req);
    }

    @PutMapping("/{id}/status")
    public QuartoResponse alterarStatus(@PathVariable Long id,
            @Valid @RequestBody QuartoStatusRequest req) {
        return quartoService.alterarStatus(id, req.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        quartoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
