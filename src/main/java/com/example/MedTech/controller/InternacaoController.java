package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.AltaRequest;
import com.example.MedTech.dto.InternacaoRequest;
import com.example.MedTech.dto.InternacaoResponse;
import com.example.MedTech.dto.PrevisaoAltaRequest;
import com.example.MedTech.service.InternacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService internacaoService;

    public InternacaoController(InternacaoService internacaoService) {
        this.internacaoService = internacaoService;
    }

    @GetMapping
    public List<InternacaoResponse> listar() {
        return internacaoService.listar();
    }

    @GetMapping("/ativas")
    public List<InternacaoResponse> listarAtivas() {
        return internacaoService.listarAtivas();
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<InternacaoResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return internacaoService.listarPorPaciente(pacienteId);
    }

    @GetMapping("/{id}")
    public InternacaoResponse buscar(@PathVariable Long id) {
        return internacaoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<InternacaoResponse> registrarEntrada(
            @Valid @RequestBody InternacaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(internacaoService.registrarEntrada(req));
    }

    @PutMapping("/{id}/previsao-alta")
    public InternacaoResponse alterarPrevisaoAlta(@PathVariable Long id,
            @Valid @RequestBody PrevisaoAltaRequest req) {
        return internacaoService.alterarPrevisaoAlta(id, req.dataPrevistaAlta());
    }

    @PostMapping("/{id}/alta")
    public InternacaoResponse registrarAlta(@PathVariable Long id,
            @RequestBody(required = false) AltaRequest req) {
        return internacaoService.registrarAlta(id, req != null ? req.dataAlta() : null);
    }
}
