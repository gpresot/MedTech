package com.example.MedTech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Páginas de gestão do hospital (acesso restrito a profissionais, ver SecurityConfig).
 * Os dados são carregados no navegador pelas APIs REST de cada módulo.
 * A página de internações fica no SecureLoginController.
 */
@Controller
public class GestaoViewController {

    @GetMapping("/pacientes/pagina")
    public String pacientes(Model model) {
        model.addAttribute("paginaAtual", "pacientes");
        return "pacientes";
    }

    @GetMapping("/quartos/pagina")
    public String quartos(Model model) {
        model.addAttribute("paginaAtual", "quartos");
        return "quartos";
    }

    @GetMapping("/procedimentos/pagina")
    public String procedimentos(Model model) {
        model.addAttribute("paginaAtual", "procedimentos");
        return "procedimentos";
    }
}
