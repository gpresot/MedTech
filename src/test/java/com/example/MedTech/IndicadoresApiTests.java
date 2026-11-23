package com.example.MedTech;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.MedTech.model.ConsultaRotina;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ConsultaRepository;
import com.example.MedTech.repository.PacienteRepository;
import com.example.MedTech.repository.ProfissionalRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Endpoints de indicadores de atendimento. */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:indicadores;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.security=WARN"
})
@Transactional
class IndicadoresApiTests {

    private static final String BASE = "/profissionais/indicadores";

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    private MockMvc mvc;
    private Profissional comConsulta;
    private Profissional semConsulta;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();

        Paciente paciente = pacienteRepository.save(new Paciente("Maria", "44444444444",
                LocalDate.of(1990, 1, 1), "31944444444", "Rua A", "maria@teste.com"));
        comConsulta = profissionalRepository.save(new MedicoClinicoGeral("Dr. Clínico", "11111111111",
                "clinico@teste.com", "31911111111", "CRM1"));
        semConsulta = profissionalRepository.save(new MedicoClinicoGeral("Dra. Livre", "22222222222",
                "livre@teste.com", "31922222222", "CRM2"));

        consultaRepository.save(new ConsultaRotina(paciente, comConsulta, LocalDate.of(2027, 3, 10),
                LocalTime.of(9, 0), "Revisão", null));
    }

    @Test
    void indicadorPorModalidadeTrazAsQuatroModalidades() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-modalidade").with(user("dr").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].modalidade").value("ROTINA"))
                .andExpect(jsonPath("$[0].total").value(1))
                .andExpect(jsonPath("$[3].modalidade").value("CIRURGIA"))
                .andExpect(jsonPath("$[3].total").value(0));
    }

    @Test
    void indicadorPorProfissionalMostraTotalEDetalhe() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-profissional").with(user("dr").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Dr. Clínico"))
                .andExpect(jsonPath("$[0].total").value(1))
                .andExpect(jsonPath("$[0].porModalidade.ROTINA").value(1))
                .andExpect(jsonPath("$[0].porModalidade.CIRURGIA").value(0));
    }

    @Test
    void periodoSemAtendimentosDevolveZero() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-modalidade")
                .param("de", "2028-01-01").param("ate", "2028-12-31")
                .with(user("dr").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(0));
    }

    @Test
    void indicadorDeUmProfissionalSemAtendimentosVemZerado() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-profissional/{id}", semConsulta.getId())
                .with(user("dr").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Dra. Livre"))
                .andExpect(jsonPath("$.total").value(0));

        mvc.perform(get(BASE + "/atendimentos-por-profissional/{id}", comConsulta.getId())
                .with(user("dr").roles("DOUTOR")))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void profissionalInexistenteDaNaoEncontrado() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-profissional/999999").with(user("dr").roles("DOUTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void paginaDeIndicadoresAbreParaDoutor() throws Exception {
        mvc.perform(get(BASE + "/pagina").with(user("dr").roles("DOUTOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("indicadores"));
    }

    @Test
    void pacienteNaoAbreAPaginaDeIndicadores() throws Exception {
        mvc.perform(get(BASE + "/pagina").with(user("maria@teste.com").roles("PACIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteNaoVeIndicadores() throws Exception {
        mvc.perform(get(BASE + "/atendimentos-por-modalidade").with(user("maria@teste.com").roles("PACIENTE")))
                .andExpect(status().isForbidden());
    }
}
