package com.example.MedTech.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SecureLoginController {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    // Dados do cadastro, na memória. A chave é o e-mail.
    // Somem quando a aplicação reinicia (igual aos usuários).
    private final Map<String, Map<String, String>> dadosUsuarios = new ConcurrentHashMap<>();

    public SecureLoginController(InMemoryUserDetailsManager userDetailsManager,
                                 PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- PÁGINAS ----------

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
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

    // ---------- CADASTRO ----------

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

        if (cpf.isBlank() || dataNascimento.isBlank() || endereco.isBlank()) {
            return erro(ra, "Preencha todos os campos obrigatórios.", "/register/paciente");
        }

        Map<String, String> extras = new HashMap<>();
        extras.put("cpf", cpf);
        extras.put("dataNascimento", dataNascimento);
        extras.put("endereco", endereco);

        return criarConta("PACIENTE", nome, email, telefone, senha, extras, ra, "/register/paciente");
    }

    @PostMapping("/register/doutor")
    public String handleRegisterDoutor(
            @RequestParam("nome") String nome,
            @RequestParam("especialidade") String especialidade,
            @RequestParam("registroProfissional") String registroProfissional,
            @RequestParam("telefone") String telefone,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            RedirectAttributes ra) {

        if (especialidade.isBlank() || registroProfissional.isBlank()) {
            return erro(ra, "Preencha todos os campos obrigatórios.", "/register/doutor");
        }

        Map<String, String> extras = new HashMap<>();
        extras.put("especialidade", especialidade);
        extras.put("registroProfissional", registroProfissional);

        return criarConta("DOUTOR", nome, email, telefone, senha, extras, ra, "/register/doutor");
    }

    // Parte comum aos dois cadastros.
    private String criarConta(String role, String nome, String email, String telefone,
                              String senha, Map<String, String> extras,
                              RedirectAttributes ra, String paginaErro) {

        String e = email.trim().toLowerCase();

        if (nome.isBlank() || e.isBlank() || telefone.isBlank() || senha.isBlank()) {
            return erro(ra, "Preencha todos os campos obrigatórios.", paginaErro);
        }
        if (!e.matches(EMAIL_REGEX)) {
            return erro(ra, "Informe um e-mail válido.", paginaErro);
        }
        if (senha.length() < 8) {
            return erro(ra, "A senha deve possuir pelo menos 8 caracteres.", paginaErro);
        }
        if (userDetailsManager.userExists(e)) {
            return erro(ra, "Já existe uma conta cadastrada com este e-mail.", paginaErro);
        }

        userDetailsManager.createUser(
                User.withUsername(e)
                        .password(passwordEncoder.encode(senha))
                        .roles(role)
                        .build());

        // Todas as chaves existem sempre, porque o template falha
        // se pedir uma chave que não está no Map.
        Map<String, String> dados = dadosVazios(e);
        dados.put("nome", valor(nome));
        dados.put("telefone", valor(telefone));
        extras.forEach((chave, v) -> dados.put(chave, valor(v)));
        dadosUsuarios.put(e, dados);

        ra.addFlashAttribute("registerSuccess",
                "Cadastro realizado com sucesso! Agora você pode fazer login.");
        return "redirect:/login";
    }

    // ---------- PERFIS ----------

    @GetMapping("/perfil/paciente")
    public String perfilPaciente() {

        return "perfil-paciente";
    }

    @GetMapping("/perfil/doutor")
    public String perfilDoutor() {
        
        return "perfil-doutor";
    }

    // ---------- AUXILIARES ----------

    private Map<String, String> dadosDe(String email) {
        Map<String, String> dados = dadosUsuarios.get(email);
        return dados != null ? dados : dadosVazios(email);
    }

    private Map<String, String> dadosVazios(String email) {
        Map<String, String> vazio = new HashMap<>();
        for (String chave : new String[] { "nome", "cpf", "dataNascimento", "telefone",
                "endereco", "especialidade", "registroProfissional" }) {
            vazio.put(chave, "—");
        }
        vazio.put("email", email);
        return vazio;
    }

    private String erro(RedirectAttributes ra, String mensagem, String pagina) {
        ra.addFlashAttribute("registerError", mensagem);
        return "redirect:" + pagina;
    }

    private String valor(String s) {
        return (s == null || s.isBlank()) ? "—" : s.trim();
    }
}