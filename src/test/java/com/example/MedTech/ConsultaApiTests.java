package com.example.MedTech;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.MedTech.dto.ConsultaRequest;
import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.PrioridadeAtendimento;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.model.AtendimentoEmergencia;
import com.example.MedTech.model.ConsultaEspecializada;
import com.example.MedTech.model.ConsultaRotina;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ConsultaRepository;
import com.example.MedTech.repository.PacienteRepository;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.service.ConsultaService;

import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:consultas;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.security=WARN"
})
@Transactional
class ConsultaApiTests {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mvc;
    private Paciente paciente;
    private Profissional profissional;
    private MedicoEspecialista especialista;
    private final LocalDate data = LocalDate.of(2027, 3, 10);
    private final LocalTime horario = LocalTime.of(14, 0);

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        paciente = pacienteRepository.save(new Paciente("Paciente teste", "12345678901",
                LocalDate.of(2000, 1, 1), "31999999999", "Rua teste", "paciente@teste.com"));
        profissional = profissionalRepository.save(new MedicoClinicoGeral("Médico teste",
                "10987654321", "medico@teste.com", "31988888888", "CRM123"));
        especialista = profissionalRepository.save(new MedicoEspecialista("Especialista teste",
                "22233344455", "especialista@teste.com", "31977777777", "CRM456",
                Especialidade.CARDIOLOGIA));
    }

    @Test
    void cadastrarEConsultarRetornaDtoSemExporEntidades() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(paciente.getId()))
                .andExpect(jsonPath("$.profissionalId").value(profissional.getId()))
                .andExpect(jsonPath("$.data").value("2027-03-10"))
                .andExpect(jsonPath("$.horario").value("14:00:00"))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.tipo").value("ROTINA"))
                .andExpect(jsonPath("$.paciente").doesNotExist())
                .andExpect(jsonPath("$.profissional").doesNotExist());

        Long id = consultaRepository.findAll().getFirst().getId();
        mvc.perform(get("/consultas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Avaliação"));
        mvc.perform(get("/consultas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void persistirTodosOsTiposMantemHeranca() {
        Long rotina = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        Long especializada = consultaService.agendar(
                requisicao(TipoConsulta.ESPECIALIZADA, horario.plusHours(1))).id();
        Long emergencia = consultaService.agendar(
                requisicao(TipoConsulta.EMERGENCIA, horario.plusHours(2))).id();
        entityManager.flush();
        entityManager.clear();

        assertInstanceOf(ConsultaRotina.class, consultaRepository.findById(rotina).orElseThrow());
        assertInstanceOf(ConsultaEspecializada.class,
                consultaRepository.findById(especializada).orElseThrow());
        assertInstanceOf(AtendimentoEmergencia.class,
                consultaRepository.findById(emergencia).orElseThrow());
    }

    @Test
    void impedirDoisTiposNoMesmoHorarioDoProfissional() throws Exception {
        consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario));
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("EMERGENCIA", "14:00:00")))
                .andExpect(status().isConflict());
        assertEquals(1, consultaRepository.count());
    }

    @Test
    void permitirMesmoHorarioParaOutroProfissional() {
        consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario));
        Profissional outro = profissionalRepository.save(new MedicoClinicoGeral("Outro médico",
                "11122233344", "outro@teste.com", "31977777777", "CRM456"));
        consultaService.agendar(new ConsultaRequest(paciente.getId(), outro.getId(),
                data, horario, "Avaliação", null, TipoConsulta.ROTINA, null, null));
        assertEquals(2, consultaRepository.count());
    }

    @Test
    void atualizarNaoConflitaComPropriaConsulta() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")))
                .andExpect(status().isOk());
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "15:00:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horario").value("15:00:00"));
    }

    @Test
    void impedirRemarcacaoParaHorarioOcupado() throws Exception {
        consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario));
        Long id = consultaService.agendar(
                requisicao(TipoConsulta.ROTINA, horario.plusHours(1))).id();
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")))
                .andExpect(status().isConflict());
        assertEquals(horario.plusHours(1), consultaRepository.findById(id).orElseThrow().getHorario());
    }

    @Test
    void cancelarPreservaHistoricoELiberaHorario() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(delete("/consultas/{id}", id)).andExpect(status().isNoContent());
        assertEquals(StatusConsulta.CANCELADA,
                consultaRepository.findById(id).orElseThrow().getStatus());
        consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario));

        mvc.perform(get("/consultas/paciente/{id}", paciente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        assertEquals(2, consultaRepository.count());
    }

    @Test
    void iniciarEConcluirRegistraObservacoes() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(post("/consultas/{id}/iniciar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        mvc.perform(post("/consultas/{id}/concluir", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Paciente orientado.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Paciente orientado."));
        mvc.perform(delete("/consultas/{id}", id)).andExpect(status().isConflict());
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "15:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void naoConcluirSemIniciarNemReabrirCancelada() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(post("/consultas/{id}/concluir", id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"observacoes\":\"Teste\"}"))
                .andExpect(status().isConflict());
        consultaService.cancelar(id);
        mvc.perform(post("/consultas/{id}/iniciar", id)).andExpect(status().isConflict());
        mvc.perform(delete("/consultas/{id}", id)).andExpect(status().isConflict());
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "15:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void naoAlterarTipoDaConsultaExistente() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ESPECIALIZADA", "14:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void validarDadosObrigatoriosETiposInvalidos() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pacienteId").exists())
                .andExpect(jsonPath("$.profissionalId").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.horario").exists())
                .andExpect(jsonPath("$.motivo").exists())
                .andExpect(jsonPath("$.tipo").exists());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("INVALIDO", "14:00:00")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "25:00:00")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00").replace("Avaliação", "   ")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/consultas/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retornar404ParaRecursosInexistentes() throws Exception {
        mvc.perform(get("/consultas/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/consultas/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/consultas/paciente/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")
                        .replace("\"pacienteId\":" + paciente.getId(), "\"pacienteId\":999999")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")
                        .replace("\"profissionalId\":" + profissional.getId(),
                                "\"profissionalId\":999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void historicoDeOutroPacienteNaoIncluiConsultasAlheias() throws Exception {
        consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario));
        Paciente outro = pacienteRepository.save(new Paciente("Outro paciente", "11122233344",
                LocalDate.of(2001, 1, 1), "31999999999", "Rua teste", "outro@teste.com"));
        mvc.perform(get("/consultas/paciente/{id}", outro.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void paginaHtmlContinuaExigindoLogin() throws Exception {
        mvc.perform(get("/consultas/pagina"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void confirmarEIniciarConsulta() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        mvc.perform(post("/consultas/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
        mvc.perform(post("/consultas/{id}/confirmar", id)).andExpect(status().isConflict());
        mvc.perform(post("/consultas/{id}/iniciar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        mvc.perform(post("/consultas/{id}/nao-compareceu", id)).andExpect(status().isConflict());
    }

    @Test
    void registrarFaltaNaoReabreConsulta() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ROTINA, horario)).id();
        consultaService.confirmar(id);
        mvc.perform(post("/consultas/{id}/nao-compareceu", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NAO_COMPARECEU"));
        mvc.perform(post("/consultas/{id}/iniciar", id)).andExpect(status().isConflict());
        mvc.perform(post("/consultas/{id}/confirmar", id)).andExpect(status().isConflict());
        mvc.perform(delete("/consultas/{id}", id)).andExpect(status().isConflict());
        mvc.perform(put("/consultas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "15:00:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void especializadaExigeMedicoDaMesmaEspecialidade() throws Exception {
        String corpoEspecializada = corpo("ESPECIALIZADA", "14:00:00")
                .replace("\"profissionalId\":" + profissional.getId(),
                        "\"profissionalId\":" + especialista.getId())
                .replace("\"tipo\":\"ESPECIALIZADA\"",
                        "\"tipo\":\"ESPECIALIZADA\",\"especialidade\":\"CARDIOLOGIA\"");
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoEspecializada.replace("CARDIOLOGIA", "ORTOPEDIA")))
                .andExpect(status().isConflict());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoEspecializada))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.especialidade").value("CARDIOLOGIA"));
    }

    @Test
    void especializadaNaoAceitaClinicoNemEspecialidadeVazia() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ESPECIALIZADA", "14:00:00")))
                .andExpect(status().isConflict());
        String corpoClinico = corpo("ESPECIALIZADA", "14:00:00")
                .replace("\"tipo\":\"ESPECIALIZADA\"",
                        "\"tipo\":\"ESPECIALIZADA\",\"especialidade\":\"CARDIOLOGIA\"");
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoClinico))
                .andExpect(status().isConflict());
    }

    @Test
    void rotinaExigeClinicoGeral() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROTINA", "14:00:00")
                        .replace("\"profissionalId\":" + profissional.getId(),
                                "\"profissionalId\":" + especialista.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void validarEspecialidadeNovamenteAntesDeIniciar() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.ESPECIALIZADA, horario)).id();
        especialista.setEspecialidade(Especialidade.ORTOPEDIA);
        mvc.perform(post("/consultas/{id}/iniciar", id)).andExpect(status().isConflict());
    }

    @Test
    void emergenciaExigePrioridadePredefinida() throws Exception {
        String corpoEmergencia = corpo("EMERGENCIA", "14:00:00");
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoEmergencia.replace(",\"prioridade\":\"ALTA\"", "")))
                .andExpect(status().isConflict());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoEmergencia.replace("\"ALTA\"", "\"INVALIDA\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(corpoEmergencia.replace("\"ALTA\"", "\"CRITICA\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("CRITICA"));
    }

    @Test
    void prioridadesSaoPersistidas() {
        int hora = 8;
        for (PrioridadeAtendimento prioridade : PrioridadeAtendimento.values()) {
            Long id = consultaService.agendar(new ConsultaRequest(paciente.getId(),
                    profissional.getId(), data, LocalTime.of(hora, 0), "Emergência",
                    null, TipoConsulta.EMERGENCIA, null, prioridade)).id();
            entityManager.flush();
            entityManager.clear();
            AtendimentoEmergencia consulta = (AtendimentoEmergencia)
                    consultaRepository.findById(id).orElseThrow();
            assertEquals(prioridade, consulta.getPrioridade());
            hora++;
        }
    }

    @Test
    void identificarEmergenciasAguardandoIniciadasEConcluidas() throws Exception {
        Long id = consultaService.agendar(requisicao(TipoConsulta.EMERGENCIA, horario)).id();
        mvc.perform(get("/consultas").param("status", "AGENDADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prioridade").value("ALTA"));
        consultaService.iniciar(id);
        mvc.perform(get("/consultas").param("status", "AGENDADA"))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/consultas").param("status", "EM_ATENDIMENTO"))
                .andExpect(jsonPath("$[0].id").value(id));
        consultaService.concluir(id, "Paciente avaliado.");
        mvc.perform(get("/consultas").param("status", "CONCLUIDA"))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/consultas").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest());
    }

    private ConsultaRequest requisicao(TipoConsulta tipo, LocalTime hora) {
        Long profissionalId = profissional.getId();
        Especialidade especialidade = null;
        PrioridadeAtendimento prioridade = null;
        if (tipo == TipoConsulta.ESPECIALIZADA) {
            profissionalId = especialista.getId();
            especialidade = Especialidade.CARDIOLOGIA;
        }
        if (tipo == TipoConsulta.EMERGENCIA) {
            prioridade = PrioridadeAtendimento.ALTA;
        }
        return new ConsultaRequest(paciente.getId(), profissionalId, data, hora,
                "Avaliação", null, tipo, especialidade, prioridade);
    }

    private String corpo(String tipo, String hora) {
        String camposExtras = "";
        if (tipo.equals("EMERGENCIA")) {
            camposExtras = ",\"prioridade\":\"ALTA\"";
        }
        return """
                {
                    "pacienteId":%d,
                    "profissionalId":%d,
                    "data":"2027-03-10",
                    "horario":"%s",
                    "motivo":"Avaliação",
                    "tipo":"%s"%s
                }
                """.formatted(paciente.getId(), profissional.getId(), hora, tipo, camposExtras);
    }
}
