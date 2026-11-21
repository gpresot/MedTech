package com.example.MedTech;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.PacienteRepository;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Cadastro (paciente e doutor) e páginas de perfil. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:cadastro;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.security=WARN"
})
@Transactional
class CadastroPerfilTests {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    // ---------- cadastro de paciente ----------

    @Test
    void cadastraPacienteAceitandoMascaraNoCpfENoTelefone() throws Exception {
        mvc.perform(post("/register/paciente").with(csrf())
                .param("nome", "Maria Souza")
                .param("cpf", "123.456.789-09")
                .param("dataNascimento", "1990-05-10")
                .param("telefone", "(31) 99999-9999")
                .param("email", "Maria@Teste.com")
                .param("endereco", "Rua A, 10")
                .param("senha", "senha1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertTrue(pacienteRepository.existsByCpf("12345678909"));
        assertTrue(usuarioRepository.existsByEmail("maria@teste.com"));
    }

    @Test
    void naoCadastraPacienteComEmailDeProfissional() throws Exception {
        profissionalRepository.save(new MedicoClinicoGeral("Dr. Fulano", "10987654321",
                "dr@teste.com", "31988888888", "CRM900"));

        mvc.perform(post("/register/paciente").with(csrf())
                .param("nome", "Maria Souza")
                .param("cpf", "12345678909")
                .param("dataNascimento", "1990-05-10")
                .param("telefone", "31999999999")
                .param("email", "dr@teste.com")
                .param("endereco", "Rua A, 10")
                .param("senha", "senha1234"))
                .andExpect(redirectedUrl("/register/paciente"))
                .andExpect(flash().attributeExists("registerError"))
                .andExpect(flash().attributeExists("dadosForm"));

        assertFalse(pacienteRepository.existsByCpf("12345678909"));
    }

    // ---------- cadastro de doutor ----------

    @Test
    void paginaDeCadastroDoDoutorListaAsEspecialidades() throws Exception {
        mvc.perform(get("/register/doutor"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("especialidades"));
    }

    @Test
    void cadastraDoutorAceitandoMascara() throws Exception {
        mvc.perform(post("/register/doutor").with(csrf())
                .param("nome", "Dra. Ana")
                .param("cpf", "111.222.333-44")
                .param("especialidade", "CARDIOLOGIA")
                .param("registroProfissional", "CRM-MG 12345")
                .param("telefone", "(31) 98888-7777")
                .param("email", "ana@teste.com")
                .param("senha", "senha1234"))
                .andExpect(redirectedUrl("/login"));

        Profissional salvo = profissionalRepository.findByEmail("ana@teste.com").orElseThrow();
        assertEquals("11122233344", salvo.getCpf());
        assertEquals("31988887777", salvo.getTelefone());
        assertTrue(usuarioRepository.existsByEmail("ana@teste.com"));
    }

    @Test
    void naoCadastraDoutorComCrmRepetido() throws Exception {
        profissionalRepository.save(new MedicoEspecialista("Dr. Beto", "55566677788",
                "beto@teste.com", "31977777777", "CRM777",
                com.example.MedTech.enums.Especialidade.NEUROLOGIA));

        mvc.perform(post("/register/doutor").with(csrf())
                .param("nome", "Dra. Ana")
                .param("cpf", "11122233344")
                .param("especialidade", "CARDIOLOGIA")
                .param("registroProfissional", "CRM777")
                .param("telefone", "31988887777")
                .param("email", "ana@teste.com")
                .param("senha", "senha1234"))
                .andExpect(redirectedUrl("/register/doutor"))
                .andExpect(flash().attributeExists("registerError"));

        assertFalse(usuarioRepository.existsByEmail("ana@teste.com"));
    }

    // ---------- perfil do doutor ----------

    @Test
    void perfilDoDoutorAbreMesmoParaMedicoQueNaoEEspecialista() throws Exception {
        profissionalRepository.save(new MedicoClinicoGeral("Dr. Clínico", "10987654321",
                "clinico@teste.com", "31988888888", "CRM901"));

        String html = mvc.perform(get("/perfil/doutor").with(user("clinico@teste.com").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil-doutor"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("Clínico geral"));
        assertTrue(html.contains("Editar perfil"));
    }

    @Test
    void doutorEditaNomeETelefone() throws Exception {
        profissionalRepository.save(new MedicoClinicoGeral("Dr. Clínico", "10987654321",
                "clinico@teste.com", "31988888888", "CRM901"));

        mvc.perform(post("/perfil/doutor").with(csrf()).with(user("clinico@teste.com").roles("DOUTOR"))
                .param("nome", "Dr. Novo Nome")
                .param("telefone", "(31) 97777-6666"))
                .andExpect(redirectedUrl("/perfil/doutor"))
                .andExpect(flash().attributeExists("perfilSucesso"));

        Profissional atualizado = profissionalRepository.findByEmail("clinico@teste.com").orElseThrow();
        assertEquals("Dr. Novo Nome", atualizado.getNome());
        assertEquals("31977776666", atualizado.getTelefone());
    }

    @Test
    void doutorNaoSalvaTelefoneInvalido() throws Exception {
        profissionalRepository.save(new MedicoClinicoGeral("Dr. Clínico", "10987654321",
                "clinico@teste.com", "31988888888", "CRM901"));

        mvc.perform(post("/perfil/doutor").with(csrf()).with(user("clinico@teste.com").roles("DOUTOR"))
                .param("nome", "Dr. Clínico")
                .param("telefone", "123"))
                .andExpect(flash().attributeExists("perfilErro"));
    }

    // ---------- perfil do paciente ----------

    @Test
    void perfilDoPacienteMostraCpfFormatadoEConsultas() throws Exception {
        pacienteRepository.save(new Paciente("Maria Souza", "12345678909",
                LocalDate.of(1990, 5, 10), "31999999999", "Rua A, 10", "maria@teste.com"));

        String html = mvc.perform(get("/perfil/paciente").with(user("maria@teste.com").roles("PACIENTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil-paciente"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("123.456.789-09"));
        assertTrue(html.contains("Nenhuma consulta agendada."));
    }

    @Test
    void pacienteEditaTelefoneEEndereco() throws Exception {
        pacienteRepository.save(new Paciente("Maria Souza", "12345678909",
                LocalDate.of(1990, 5, 10), "31999999999", "Rua A, 10", "maria@teste.com"));

        mvc.perform(post("/perfil/paciente").with(csrf()).with(user("maria@teste.com").roles("PACIENTE"))
                .param("telefone", "(31) 96666-5555")
                .param("endereco", "Rua B, 20"))
                .andExpect(redirectedUrl("/perfil/paciente"))
                .andExpect(flash().attributeExists("perfilSucesso"));

        Paciente atualizada = pacienteRepository.findByEmail("maria@teste.com").orElseThrow();
        assertEquals("31966665555", atualizada.getTelefone());
        assertEquals("Rua B, 20", atualizada.getEndereco());
    }

    @Test
    void pacienteNaoAcessaPerfilDeDoutor() throws Exception {
        mvc.perform(get("/perfil/doutor").with(user("maria@teste.com").roles("PACIENTE")))
                .andExpect(status().isForbidden());
    }
}
