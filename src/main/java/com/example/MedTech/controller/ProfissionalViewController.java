
package com.example.MedTech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.MedTech.model.Profissional;
import com.example.MedTech.service.ProfissionalService;

@Controller
@RequestMapping("/profissionais")
public class ProfissionalViewController {

    private final ProfissionalService profissionalService;

    public ProfissionalViewController(ProfissionalService profissionalService) {
        this.profissionalService = profissionalService;
    }

    // ---------- LISTAGEM DE PROFISSIONAIS ----------

    @GetMapping("/pagina")
    public String listarProfissionais(Model model) {

        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("paginaAtual", "profissionais");

        return "profissionais";
    }

    // ---------- EDIÇÃO DE PROFISSIONAL ----------

    @GetMapping("/editar/{id}")
    public String editarProfissional(@PathVariable Long id, Model model) {

        Profissional profissional = profissionalService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado."));

        model.addAttribute("profissional", profissional);
        model.addAttribute("paginaAtual", "profissionais");

        return "editar-profissional";
    }
}
