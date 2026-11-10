package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.ProcedimentoResponse;
import com.example.MedTech.dto.ProfissionalDTO;
import com.example.MedTech.dto.ProfissionalResponse;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.factory.ProfissionalFactory;
import com.example.MedTech.service.ProfissionalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    public ProfissionalController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    @GetMapping
    public List<ProfissionalResponse> listarTodos() {
        return profissionalService.listarTodos().stream().map(ProfissionalResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscarPorId(@PathVariable Long id) {
        return profissionalService.buscarPorId(id)
                .map(ProfissionalResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> cadastrar(@Valid @RequestBody ProfissionalDTO dto) {
        var salvo = profissionalService.salvar(ProfissionalFactory.criar(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfissionalResponse.de(salvo));
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalDTO dto) {
        return ProfissionalResponse.de(profissionalService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        profissionalService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Médicos especialistas disponíveis em uma área de atuação. */
    @GetMapping("/especialidade/{especialidade}")
    public List<ProfissionalResponse> buscarPorEspecialidade(@PathVariable Especialidade especialidade) {
        return profissionalService.buscarPorEspecialidade(especialidade).stream()
                .map(ProfissionalResponse::de).toList();
    }

    /** Procedimentos realizados/agendados de um cirurgião (data, paciente e descrição). */
    @GetMapping("/{id}/procedimentos")
    public List<ProcedimentoResponse> procedimentosDoCirurgiao(@PathVariable Long id,
            @RequestParam(required = false) StatusProcedimento status) {
        return profissionalService.listarProcedimentosDoCirurgiao(id, status);
    }
}
