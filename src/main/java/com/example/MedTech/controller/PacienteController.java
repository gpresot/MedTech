package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.HistoricoMedicoResponse;
import com.example.MedTech.dto.InformacaoRequest;
import com.example.MedTech.dto.PacienteRequest;
import com.example.MedTech.dto.PacienteResponse;
import com.example.MedTech.service.HistoricoMedicoService;
import com.example.MedTech.service.PacienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final HistoricoMedicoService historicoService;

    public PacienteController(PacienteService pacienteService,
            HistoricoMedicoService historicoService) {
        this.pacienteService = pacienteService;
        this.historicoService = historicoService;
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        return pacienteService.listar();
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        return pacienteService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.cadastrar(req));
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id,
            @Valid @RequestBody PacienteRequest req) {
        return pacienteService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pacienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- HISTÓRICO MÉDICO ----------

    @GetMapping("/{id}/historico")
    public HistoricoMedicoResponse consultarHistorico(@PathVariable Long id) {
        return historicoService.consultar(id);
    }

    @PostMapping("/{id}/historico/informacoes")
    public ResponseEntity<HistoricoMedicoResponse> adicionarInformacao(
            @PathVariable Long id, @Valid @RequestBody InformacaoRequest req) {
        pacienteService.adicionarInformacao(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(historicoService.consultar(id));
    }
}
