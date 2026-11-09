package com.example.MedTech;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.repository.PacienteRepository;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.repository.QuartoRepository;
import com.example.MedTech.enums.TipoQuarto;
import com.example.MedTech.model.Quarto;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:internacoes_quartos_cirurgias;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.springframework.security=WARN"
})
@Transactional
class InternacaoQuartoCirurgiaApiTests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private QuartoRepository quartoRepository;

    private MockMvc mvc;
    private Paciente paciente;
    private Paciente segundoPaciente;
    private MedicoClinicoGeral medico;
    private Cirurgiao cirurgiao;
    private Quarto quarto;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        paciente = pacienteRepository.save(new Paciente("Paciente um", "11111111111",
                LocalDate.of(1990, 1, 1), "31999990001", "Rua A", "um@teste.com"));
        segundoPaciente = pacienteRepository.save(new Paciente("Paciente dois", "22222222222",
                LocalDate.of(1992, 2, 2), "31999990002", "Rua B", "dois@teste.com"));
        medico = profissionalRepository.save(new MedicoClinicoGeral("Médico", "33333333333",
                "medico@teste.com", "31999990003", "CRM111"));
        cirurgiao = profissionalRepository.save(new Cirurgiao("Cirurgião", "44444444444",
                "cirurgiao@teste.com", "31999990004", "CRM222"));
        quarto = quartoRepository.save(new Quarto(101, 1, TipoQuarto.UTI, 1));
    }

    @Test
    void quartoRetornaTipoERejeitaCapacidadeAcimaDoLimite() throws Exception {
        mvc.perform(get("/quartos/{id}", quarto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("UTI"))
                .andExpect(jsonPath("$.capacidadeMaxima").value(1));

        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":102,\"andar\":1,\"capacidadeMaxima\":2,\"tipo\":\"UTI\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":103,\"andar\":1,\"capacidadeMaxima\":3,\"tipo\":\"APARTAMENTO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":104,\"andar\":1,\"capacidadeMaxima\":7,\"tipo\":\"ENFERMARIA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void quartoSemTipoInformadoAssumeEnfermaria() throws Exception {
        mvc.perform(post("/quartos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":102,\"andar\":1,\"capacidadeMaxima\":6}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENFERMARIA"));
    }

    @Test
    void altaLiberaVagaParaNovaInternacao() throws Exception {
        long primeiraInternacao = registrarInternacao(paciente.getId(), quarto.getId());

        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpoInternacao(segundoPaciente.getId(), quarto.getId())))
                .andExpect(status().isConflict());

        mvc.perform(post("/internacoes/{id}/alta", primeiraInternacao)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataAlta\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false));

        mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpoInternacao(segundoPaciente.getId(), quarto.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true));

        mvc.perform(get("/quartos/{id}", quarto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ocupacaoAtual").value(1));
    }

    @Test
    void cirurgiaRegistraTipoEHorariosEfetivosEExigeObservacaoPreOperatoria() throws Exception {
        long internacaoId = registrarInternacao(paciente.getId(), quarto.getId());
        String cirurgia = "{\"internacaoId\":" + internacaoId
                + ",\"cirurgiaoId\":" + cirurgiao.getId()
                + ",\"descricao\":\"Procedimento teste\",\"tipo\":\"EMERGENCIA\""
                + ",\"dataHora\":\"" + LocalDateTime.now().plusDays(30) + "\"}";

        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(cirurgia))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("EMERGENCIA"))
                .andExpect(jsonPath("$.dataHoraInicioEfetivo").value(org.hamcrest.Matchers.nullValue()));

        long procedimentoId = procedimentoIdPorInternacao(internacaoId);
        mvc.perform(post("/procedimentos/{id}/concluir", procedimentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Tentativa prematura\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/procedimentos/{id}/iniciar", procedimentoId))
                .andExpect(status().isConflict());

        mvc.perform(put("/procedimentos/{id}/pre-operatorio", procedimentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Avaliação pré-operatória realizada\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/procedimentos/{id}/iniciar", procedimentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.dataHoraInicioEfetivo").exists());

        mvc.perform(post("/procedimentos/{id}/concluir", procedimentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"observacoes\":\"Pós-operatório registrado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.dataHoraFimEfetivo").exists());
    }

    @Test
    void cirurgiaSemTipoInformadoAssumeEletiva() throws Exception {
        long internacaoId = registrarInternacao(paciente.getId(), quarto.getId());
        String corpo = "{\"internacaoId\":" + internacaoId
                + ",\"cirurgiaoId\":" + cirurgiao.getId()
                + ",\"descricao\":\"Procedimento eletivo\",\"dataHora\":\""
                + LocalDateTime.now().plusDays(45) + "\"}";

        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ELETIVA"));
    }

    @Test
    void profissionalQueNaoEhCirurgiaoNaoPodeSerResponsavel() throws Exception {
        long internacaoId = registrarInternacao(paciente.getId(), quarto.getId());
        String corpo = "{\"internacaoId\":" + internacaoId
                + ",\"cirurgiaoId\":" + medico.getId()
                + ",\"descricao\":\"Procedimento teste\",\"dataHora\":\""
                + LocalDateTime.now().plusDays(30) + "\"}";

        mvc.perform(post("/procedimentos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    private long registrarInternacao(Long pacienteId, Long quartoId) throws Exception {
        String json = corpoInternacao(pacienteId, quartoId);
        String resposta = mvc.perform(post("/internacoes").contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extrairPrimeiroId(resposta);
    }

    private long procedimentoIdPorInternacao(Long internacaoId) throws Exception {
        String resposta = mvc.perform(get("/procedimentos/internacao/{id}", internacaoId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return extrairPrimeiroId(resposta);
    }

    private long extrairPrimeiroId(String json) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\\"id\\\"\\s*:\\s*(\\d+)")
                .matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("A resposta não contém um id: " + json);
        }
        return Long.parseLong(matcher.group(1));
    }

    private String corpoInternacao(Long pacienteId, Long quartoId) {
        return "{\"pacienteId\":" + pacienteId + ",\"profissionalId\":" + medico.getId()
                + ",\"quartoId\":" + quartoId + ",\"dataEntrada\":\"" + LocalDate.now()
                + "\",\"dataPrevistaAlta\":\"" + LocalDate.now().plusDays(10) + "\"}";
    }
}
