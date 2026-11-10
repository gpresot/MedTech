package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.ConsultaRequest;
import com.example.MedTech.dto.ConsultaResponse;
import com.example.MedTech.dto.ObservacoesRequest;
import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.dto.EncaminhamentoRequest;
import com.example.MedTech.service.ConsultaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<ConsultaResponse> listar(@RequestParam(required = false) StatusConsulta status,
            @RequestParam(required = false) TipoConsulta tipo) {
        return consultaService.listarFiltrado(status, tipo);
    }

    @GetMapping("/{id}")
    public ConsultaResponse buscar(@PathVariable Long id) {
        return consultaService.buscar(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<ConsultaResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return consultaService.listarPorPaciente(pacienteId);
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody ConsultaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.agendar(req));
    }

    @PutMapping("/{id}")
    public ConsultaResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody ConsultaRequest req) {
        return consultaService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        consultaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/iniciar")
    public ConsultaResponse iniciar(@PathVariable Long id) {
        return consultaService.iniciar(id);
    }

    @PostMapping("/{id}/encaminhar")
    public ConsultaResponse encaminhar(@PathVariable Long id,
            @Valid @RequestBody EncaminhamentoRequest req) {
        return consultaService.encaminhar(id, req.especialidade());
    }

    @PostMapping("/{id}/confirmar")
    public ConsultaResponse confirmar(@PathVariable Long id) {
        return consultaService.confirmar(id);
    }

    @PostMapping("/{id}/nao-compareceu")
    public ConsultaResponse registrarNaoComparecimento(@PathVariable Long id) {
        return consultaService.registrarNaoComparecimento(id);
    }

    @PostMapping("/{id}/concluir")
    public ConsultaResponse concluir(@PathVariable Long id,
            @Valid @RequestBody ObservacoesRequest req) {
        return consultaService.concluir(id, req.observacoes());
    }
}
