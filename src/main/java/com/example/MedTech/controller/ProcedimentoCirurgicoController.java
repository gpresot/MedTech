package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.ObservacoesRequest;
import com.example.MedTech.dto.ProcedimentoRequest;
import com.example.MedTech.dto.ProcedimentoResponse;
import com.example.MedTech.service.ProcedimentoCirurgicoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoCirurgicoController {

    private final ProcedimentoCirurgicoService procedimentoService;

    public ProcedimentoCirurgicoController(ProcedimentoCirurgicoService procedimentoService) {
        this.procedimentoService = procedimentoService;
    }

    @GetMapping
    public List<ProcedimentoResponse> listar() {
        return procedimentoService.listar();
    }

    @GetMapping("/internacao/{internacaoId}")
    public List<ProcedimentoResponse> listarPorInternacao(@PathVariable Long internacaoId) {
        return procedimentoService.listarPorInternacao(internacaoId);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<ProcedimentoResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return procedimentoService.listarPorPaciente(pacienteId);
    }

    @GetMapping("/{id}")
    public ProcedimentoResponse buscar(@PathVariable Long id) {
        return procedimentoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProcedimentoResponse> agendar(@Valid @RequestBody ProcedimentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(procedimentoService.agendar(req));
    }

    @PutMapping("/{id}/pre-operatorio")
    public ProcedimentoResponse registrarPreOperatorio(@PathVariable Long id,
            @Valid @RequestBody ObservacoesRequest req) {
        return procedimentoService.registrarObservacoesPreOperatorias(id, req.observacoes());
    }

    @PostMapping("/{id}/iniciar")
    public ProcedimentoResponse iniciar(@PathVariable Long id) {
        return procedimentoService.iniciar(id);
    }

    @PostMapping("/{id}/concluir")
    public ProcedimentoResponse concluir(@PathVariable Long id,
            @Valid @RequestBody ObservacoesRequest req) {
        return procedimentoService.concluir(id, req.observacoes());
    }

    @PostMapping("/{id}/cancelar")
    public ProcedimentoResponse cancelar(@PathVariable Long id) {
        return procedimentoService.cancelar(id);
    }
}
