package com.example.MedTech.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.MedTech.dto.PacienteResponse;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.service.HistoricoMedicoService;
import com.example.MedTech.service.PacienteService;

/**
 * Página de perfil do paciente logado. Tudo é buscado pelo e-mail da sessão,
 * nunca por um id vindo da tela, para o paciente só enxergar os próprios dados.
 */
@Controller
@RequestMapping("/perfil/paciente")
public class PacienteViewController {

    private final PacienteService pacienteService;
    private final HistoricoMedicoService historicoService;

    public PacienteViewController(PacienteService pacienteService,
            HistoricoMedicoService historicoService) {
        this.pacienteService = pacienteService;
        this.historicoService = historicoService;
    }

    @GetMapping
    public String perfil(Authentication authentication, Model model) {

        PacienteResponse paciente;

        try {
            paciente = pacienteService.buscarPorEmail(authentication.getName());
        } catch (RecursoNaoEncontradoException e) {
            // Conta antiga: tem login, mas ainda não tem os dados de paciente.
            return "redirect:/perfil/paciente/completar";
        }

        model.addAttribute("paciente", paciente);
        model.addAttribute("historico", historicoService.consultar(paciente.id()));
        model.addAttribute("paginaAtual", "perfil");

        return "perfil-paciente";
    }

    @PostMapping
    public String atualizarContato(
            Authentication authentication,
            @RequestParam("telefone") String telefone,
            @RequestParam("endereco") String endereco,
            RedirectAttributes ra) {

        // Aceita máscara no telefone: guarda só os números.
        String telefoneNumeros = telefone.replaceAll("\\D", "");

        try {
            pacienteService.atualizarContatoPorEmail(
                    authentication.getName(), telefoneNumeros, endereco);

            ra.addFlashAttribute("perfilSucesso", "Dados atualizados com sucesso!");

        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            ra.addFlashAttribute("perfilErro", e.getMessage());
        }

        return "redirect:/perfil/paciente";
    }

    // ---------- COMPLETAR CADASTRO (contas antigas) ----------

    @GetMapping("/completar")
    public String completar(Authentication authentication, Model model) {

        try {
            pacienteService.buscarPorEmail(authentication.getName());
            return "redirect:/perfil/paciente";
        } catch (RecursoNaoEncontradoException e) {
            model.addAttribute("email", authentication.getName());
            model.addAttribute("paginaAtual", "perfil");
            return "completar-cadastro-paciente";
        }
    }

    @PostMapping("/completar")
    public String salvarCadastro(
            Authentication authentication,
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("dataNascimento") String dataNascimento,
            @RequestParam("telefone") String telefone,
            @RequestParam("endereco") String endereco,
            RedirectAttributes ra) {

        LocalDate nascimento;

        try {
            nascimento = LocalDate.parse(dataNascimento);
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("perfilErro", "Informe uma data de nascimento válida.");
            return "redirect:/perfil/paciente/completar";
        }

        try {
            pacienteService.completarCadastro(
                    authentication.getName(), nome,
                    cpf.replaceAll("\\D", ""), nascimento,
                    telefone.replaceAll("\\D", ""), endereco);

        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("perfilErro", e.getMessage());
            return "redirect:/perfil/paciente/completar";
        }

        ra.addFlashAttribute("perfilSucesso", "Cadastro completo! Bem-vindo ao MedTech.");

        return "redirect:/perfil/paciente";
    }
}
