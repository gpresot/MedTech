
package com.example.MedTech.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.service.CadastroService;

@Controller
public class SecureLoginController {

    private static final String EMAIL_REGEX =
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final CadastroService cadastroService;
    private final ProfissionalRepository profissionalRepository;

    public SecureLoginController(
            CadastroService cadastroService,
            ProfissionalRepository profissionalRepository) {

        this.cadastroService = cadastroService;
        this.profissionalRepository = profissionalRepository;
    }

    // ---------- PÁGINAS ----------

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String register() {
        return "redirect:/home";
    }

    @GetMapping("/register/paciente")
    public String registerPaciente() {
        return "register-paciente";
    }

    @GetMapping("/register/doutor")
    public String registerDoutor() {
        return "register-doutor";
    }

    // ---------- CADASTRO DE PACIENTE ----------

    @PostMapping("/register/paciente")
    public String handleRegisterPaciente(
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("dataNascimento") String dataNascimento,
            @RequestParam("telefone") String telefone,
            @RequestParam("email") String email,
            @RequestParam("endereco") String endereco,
            @RequestParam("senha") String senha,
            RedirectAttributes ra) {

        if (nome.isBlank() || cpf.isBlank() || dataNascimento.isBlank()
                || telefone.isBlank() || email.isBlank()
                || endereco.isBlank() || senha.isBlank()) {

            return erro(ra, "Preencha todos os campos obrigatórios.",
                    "/register/paciente");
        }

        // Aceita máscara (999.999.999-99, (31) 99999-9999): guarda só os números.
        String cpfNumeros = cpf.replaceAll("\\D", "");
        String telefoneNumeros = telefone.replaceAll("\\D", "");

        if (cpfNumeros.length() != 11) {
            return erro(ra, "O CPF deve possuir 11 números.",
                    "/register/paciente");
        }

        if (telefoneNumeros.length() < 10 || telefoneNumeros.length() > 11) {
            return erro(ra, "O telefone deve possuir 10 ou 11 números (com DDD).",
                    "/register/paciente");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!emailNormalizado.matches(EMAIL_REGEX)) {
            return erro(ra, "Informe um e-mail válido.", "/register/paciente");
        }

        if (senha.length() < 8) {
            return erro(ra, "A senha deve possuir pelo menos 8 caracteres.",
                    "/register/paciente");
        }

        LocalDate nascimento;

        try {
            nascimento = LocalDate.parse(dataNascimento);
        } catch (DateTimeParseException e) {
            return erro(ra, "Informe uma data de nascimento válida.",
                    "/register/paciente");
        }

        if (!nascimento.isBefore(LocalDate.now())) {
            return erro(ra, "A data de nascimento deve estar no passado.",
                    "/register/paciente");
        }

        try {
            cadastroService.cadastrarPaciente(
                    nome, cpfNumeros, nascimento, telefoneNumeros,
                    endereco, emailNormalizado, senha);

        } catch (IllegalArgumentException e) {
            return erro(ra, e.getMessage(), "/register/paciente");
        }

        ra.addFlashAttribute(
                "registerSuccess",
                "Cadastro realizado com sucesso! Agora você pode fazer login.");

        return "redirect:/login";
    }

    // ---------- CADASTRO DE DOUTOR ----------

    @PostMapping("/register/doutor")
    public String handleRegisterDoutor(
            @RequestParam("nome") String nome,
            @RequestParam("cpf") String cpf,
            @RequestParam("especialidade") String especialidade,
            @RequestParam("registroProfissional") String registroProfissional,
            @RequestParam("telefone") String telefone,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            RedirectAttributes ra) {

        if (nome.isBlank() || cpf.isBlank()
                || especialidade.isBlank()
                || registroProfissional.isBlank()
                || telefone.isBlank() || email.isBlank()
                || senha.isBlank()) {

            return erro(ra, "Preencha todos os campos obrigatórios.",
                    "/register/doutor");
        }

        if (cpf.length() != 11
                || !cpf.chars().allMatch(Character::isDigit)) {

            return erro(ra, "O CPF deve possuir 11 números.",
                    "/register/doutor");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!emailNormalizado.matches(EMAIL_REGEX)) {
            return erro(ra, "Informe um e-mail válido.", "/register/doutor");
        }

        if (senha.length() < 8) {
            return erro(ra, "A senha deve possuir pelo menos 8 caracteres.",
                    "/register/doutor");
        }

        try {
            cadastroService.cadastrarDoutor(
                    nome,
                    cpf,
                    especialidade,
                    registroProfissional,
                    telefone,
                    emailNormalizado,
                    senha);

        } catch (IllegalArgumentException e) {
            return erro(ra, e.getMessage(), "/register/doutor");
        }

        ra.addFlashAttribute(
                "registerSuccess",
                "Cadastro realizado com sucesso! Agora você pode fazer login.");

        return "redirect:/login";
    }

    // ---------- PERFIS ----------

    @GetMapping("/perfil/doutor")
    public String perfilDoutor(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Profissional profissional = profissionalRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado para este usuário."));

        model.addAttribute("profissional", profissional);
        model.addAttribute("paginaAtual", "perfil");

        return "perfil-doutor";
    }

    // ---------- AUXILIARES ----------

    private String erro(
            RedirectAttributes ra,
            String mensagem,
            String pagina) {

        ra.addFlashAttribute("registerError", mensagem);

        return "redirect:" + pagina;
    }

    // ---------- CONSULTAS E INTERNAÇÕES ----------

    @GetMapping("/consultas/pagina")
    public String consultas(
            Authentication authentication,
            Model model) {

        System.out.println("ACESSANDO CONSULTAS");

        if (authentication != null) {
            System.out.println("USUARIO: " + authentication.getName());
            System.out.println("AUTENTICADO: " + authentication.isAuthenticated());
        } else {
            System.out.println("USUARIO NAO AUTENTICADO");
        }

        model.addAttribute("paginaAtual", "consultas");

        return "consultas";
    }

    @GetMapping("/internacoes/pagina")
    public String internacoes(Model model) {

        model.addAttribute("paginaAtual", "internacoes");

        return "internacoes";
    }

    // ---------- CADASTRAR PROFISSIONAL ----------

    @GetMapping("/profissionais/cadastrar")
    public String cadastrarProfissional() {

        return "cadastrar-profissional";
    }
}
