
package com.example.MedTech.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.MedTech.config.Rotulos;
import com.example.MedTech.dto.ProfissionalResponse;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.service.CadastroService;
import com.example.MedTech.service.ConsultaService;
import com.example.MedTech.service.ProfissionalService;
import com.example.MedTech.util.AgendaConsultas;

@Controller
public class SecureLoginController {

    private static final Logger log = LoggerFactory.getLogger(SecureLoginController.class);

    private static final String EMAIL_REGEX =
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final CadastroService cadastroService;
    private final ProfissionalRepository profissionalRepository;
    private final ProfissionalService profissionalService;
    private final ConsultaService consultaService;
    private final Rotulos rotulos;
    private final String codigoConviteDoutor;

    public SecureLoginController(
            CadastroService cadastroService,
            ProfissionalRepository profissionalRepository,
            ProfissionalService profissionalService,
            ConsultaService consultaService,
            Rotulos rotulos,
            @Value("${app.codigo-convite-doutor:}") String codigoConviteDoutor) {

        this.cadastroService = cadastroService;
        this.profissionalRepository = profissionalRepository;
        this.profissionalService = profissionalService;
        this.consultaService = consultaService;
        this.rotulos = rotulos;
        this.codigoConviteDoutor = codigoConviteDoutor == null ? "" : codigoConviteDoutor.trim();
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
    public String registerDoutor(Model model) {
        // A lista vem do enum, então nunca fica diferente do que o backend aceita.
        model.addAttribute("especialidades", Especialidade.values());
        model.addAttribute("exigeConvite", !codigoConviteDoutor.isBlank());
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

        // Em caso de erro o formulário volta preenchido (a senha nunca volta).
        Map<String, String> dados = Map.of(
                "nome", nome, "cpf", cpf, "dataNascimento", dataNascimento,
                "telefone", telefone, "email", email, "endereco", endereco);

        if (nome.isBlank() || cpf.isBlank() || dataNascimento.isBlank()
                || telefone.isBlank() || email.isBlank()
                || endereco.isBlank() || senha.isBlank()) {

            return erro(ra, "Preencha todos os campos obrigatórios.",
                    "/register/paciente", dados);
        }

        // Aceita máscara (999.999.999-99, (31) 99999-9999): guarda só os números.
        String cpfNumeros = cpf.replaceAll("\\D", "");
        String telefoneNumeros = telefone.replaceAll("\\D", "");

        if (cpfNumeros.length() != 11) {
            return erro(ra, "O CPF deve possuir 11 números.",
                    "/register/paciente", dados);
        }

        if (telefoneNumeros.length() < 10 || telefoneNumeros.length() > 11) {
            return erro(ra, "O telefone deve possuir 10 ou 11 números (com DDD).",
                    "/register/paciente", dados);
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!emailNormalizado.matches(EMAIL_REGEX)) {
            return erro(ra, "Informe um e-mail válido.", "/register/paciente", dados);
        }

        if (senha.length() < 8) {
            return erro(ra, "A senha deve possuir pelo menos 8 caracteres.",
                    "/register/paciente", dados);
        }

        LocalDate nascimento;

        try {
            nascimento = LocalDate.parse(dataNascimento);
        } catch (DateTimeParseException e) {
            return erro(ra, "Informe uma data de nascimento válida.",
                    "/register/paciente", dados);
        }

        if (!nascimento.isBefore(LocalDate.now())) {
            return erro(ra, "A data de nascimento deve estar no passado.",
                    "/register/paciente", dados);
        }

        try {
            cadastroService.cadastrarPaciente(
                    nome, cpfNumeros, nascimento, telefoneNumeros,
                    endereco, emailNormalizado, senha);

        } catch (IllegalArgumentException e) {
            return erro(ra, e.getMessage(), "/register/paciente", dados);

        } catch (DataIntegrityViolationException e) {
            log.warn("Cadastro de paciente recusado pelo banco", e);
            return erro(ra, "O banco recusou o cadastro: " + motivo(e),
                    "/register/paciente", dados);

        } catch (DataAccessException e) {
            log.error("Erro de banco ao cadastrar paciente", e);
            return erro(ra, "Não foi possível salvar o cadastro agora. Tente novamente em instantes.",
                    "/register/paciente", dados);
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
            @RequestParam(value = "codigoConvite", required = false) String codigoConvite,
            RedirectAttributes ra) {

        Map<String, String> dados = Map.of(
                "nome", nome, "cpf", cpf, "especialidade", especialidade,
                "registroProfissional", registroProfissional,
                "telefone", telefone, "email", email);

        if (nome.isBlank() || cpf.isBlank()
                || especialidade.isBlank()
                || registroProfissional.isBlank()
                || telefone.isBlank() || email.isBlank()
                || senha.isBlank()) {

            return erro(ra, "Preencha todos os campos obrigatórios.",
                    "/register/doutor", dados);
        }

        if (!conviteValido(codigoConvite)) {
            return erro(ra, "Código de convite inválido.", "/register/doutor", dados);
        }

        // Aceita máscara no CPF e no telefone: guarda só os números.
        String cpfNumeros = cpf.replaceAll("\\D", "");
        String telefoneNumeros = telefone.replaceAll("\\D", "");

        if (cpfNumeros.length() != 11) {
            return erro(ra, "O CPF deve possuir 11 números.",
                    "/register/doutor", dados);
        }

        if (telefoneNumeros.length() < 10 || telefoneNumeros.length() > 11) {
            return erro(ra, "O telefone deve possuir 10 ou 11 números (com DDD).",
                    "/register/doutor", dados);
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!emailNormalizado.matches(EMAIL_REGEX)) {
            return erro(ra, "Informe um e-mail válido.", "/register/doutor", dados);
        }

        if (senha.length() < 8) {
            return erro(ra, "A senha deve possuir pelo menos 8 caracteres.",
                    "/register/doutor", dados);
        }

        try {
            cadastroService.cadastrarDoutor(
                    nome,
                    cpfNumeros,
                    especialidade,
                    registroProfissional,
                    telefoneNumeros,
                    emailNormalizado,
                    senha);

        } catch (IllegalArgumentException e) {
            return erro(ra, e.getMessage(), "/register/doutor", dados);

        } catch (DataIntegrityViolationException e) {
            log.warn("Cadastro de doutor recusado pelo banco", e);
            return erro(ra, "O banco recusou o cadastro: " + motivo(e),
                    "/register/doutor", dados);

        } catch (DataAccessException e) {
            log.error("Erro de banco ao cadastrar doutor", e);
            return erro(ra, "Não foi possível salvar o cadastro agora. Tente novamente em instantes.",
                    "/register/doutor", dados);
        }

        ra.addFlashAttribute(
                "registerSuccess",
                "Cadastro realizado com sucesso! Agora você pode fazer login.");

        return "redirect:/login";
    }

    /** Primeira linha da causa real (ex.: coluna NOT NULL, valor longo demais). */
    private String motivo(DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause().getMessage();
        return msg == null ? "violação de restrição" : msg.lines().findFirst().orElse(msg);
    }

    /** Sem código configurado o cadastro continua aberto; com código, ele é exigido. */
    private boolean conviteValido(String informado) {
        if (codigoConviteDoutor.isBlank()) {
            return true;
        }
        byte[] esperado = codigoConviteDoutor.getBytes(StandardCharsets.UTF_8);
        byte[] recebido = (informado == null ? "" : informado.trim()).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, recebido);
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

        ProfissionalResponse dados = ProfissionalResponse.de(profissional);

        AgendaConsultas agenda = AgendaConsultas.de(
                consultaService.listarPorProfissional(dados.id()));

        model.addAttribute("profissional", dados);
        model.addAttribute("cargo", cargo(dados));
        model.addAttribute("proximasConsultas", agenda.proximas());
        model.addAttribute("consultasAnteriores", agenda.anteriores());
        model.addAttribute("paginaAtual", "perfil");

        return "perfil-doutor";
    }

    /** O doutor edita só nome e telefone (e-mail é o login; CPF e CRM não mudam aqui). */
    @PostMapping("/perfil/doutor")
    public String atualizarPerfilDoutor(
            Authentication authentication,
            @RequestParam("nome") String nome,
            @RequestParam("telefone") String telefone,
            RedirectAttributes ra) {

        try {
            profissionalService.atualizarContatoPorEmail(
                    authentication.getName(), nome, telefone.replaceAll("\\D", ""));

            ra.addFlashAttribute("perfilSucesso", "Dados atualizados com sucesso!");

        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            ra.addFlashAttribute("perfilErro", e.getMessage());
        }

        return "redirect:/perfil/doutor";
    }

    private String cargo(ProfissionalResponse p) {
        return switch (p.tipo()) {
            case "ESPECIALISTA" -> "Médico especialista"
                    + (p.especialidade() != null ? " — " + rotulos.de(p.especialidade()) : "");
            case "CLINICO" -> "Clínico geral";
            case "CIRURGIAO" -> "Cirurgião";
            default -> "Profissional";
        };
    }

    // ---------- AUXILIARES ----------

    private String erro(
            RedirectAttributes ra,
            String mensagem,
            String pagina,
            Map<String, String> dados) {

        ra.addFlashAttribute("registerError", mensagem);
        ra.addFlashAttribute("dadosForm", dados);

        return "redirect:" + pagina;
    }

    // ---------- CONSULTAS E INTERNAÇÕES ----------

    @GetMapping("/consultas/pagina")
    public String consultas(Model model) {

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
