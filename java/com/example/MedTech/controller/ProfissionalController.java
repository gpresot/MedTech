
package com.example.MedTech.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.MedTech.dto.ProfissionalDTO;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.factory.ProfissionalFactory;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Profissional;
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
    public List<Profissional> listarTodos() {
        return profissionalService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscPorId(@PathVariable Long id) {
        return profissionalService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Profissional cadastrar(@Valid @RequestBody ProfissionalDTO dto) {
        Profissional profissional = ProfissionalFactory.criar(dto);
        return profissionalService.salvar(profissional);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (profissionalService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        profissionalService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profissional> atualizar(
            @PathVariable Long id,
            @RequestBody ProfissionalDTO dto) {

        var profissionalExistente = profissionalService.buscarPorId(id);

        if (profissionalExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Profissional profissional = profissionalExistente.get();

        profissional.setNome(dto.getNome());
        profissional.setCpf(dto.getCpf());
        profissional.setEmail(dto.getEmail());
        profissional.setTelefone(dto.getTelefone());
        profissional.setCrm(dto.getCrm());

        if (profissional instanceof MedicoEspecialista especialista) {
            especialista.setEspecialidade(dto.getEspecialidade());
        }

        Profissional atualizado = profissionalService.salvar(profissional);

        return ResponseEntity.ok(atualizado);
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<Profissional>> buscarPorEspecialidade(@PathVariable Especialidade especialidade) {
        List<Profissional> profissionais = profissionalService.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(profissionais);
    }

}
