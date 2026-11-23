package com.example.MedTech.unit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.MedTech.dto.AtendimentosPorModalidadeResponse;
import com.example.MedTech.dto.AtendimentosPorProfissionalResponse;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.model.Atendimento;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.Consulta;
import com.example.MedTech.model.ConsultaEspecializada;
import com.example.MedTech.model.ConsultaRotina;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.util.IndicadoresAtendimento;

import static org.junit.jupiter.api.Assertions.*;

/** Indicadores de atendimento por profissional e por modalidade (sem Spring e sem banco). */
class IndicadoresAtendimentoTest {

    private static final LocalDate DIA = LocalDate.of(2027, 3, 10);

    private Profissional clinico;
    private Profissional cardiologista;
    private Cirurgiao cirurgiao;
    private final Paciente paciente = new Paciente("Maria", "44444444444",
            LocalDate.of(1990, 1, 1), "31944444444", "Rua A", "maria@teste.com");

    @BeforeEach
    void criarProfissionais() {
        clinico = new MedicoClinicoGeral("Dr. Clínico", "11111111111", "c@teste.com", "31911111111", "CRM1");
        clinico.setId(1L);
        cardiologista = new MedicoEspecialista("Dra. Cardio", "22222222222", "e@teste.com",
                "31922222222", "CRM2", Especialidade.CARDIOLOGIA);
        cardiologista.setId(2L);
        cirurgiao = new Cirurgiao("Dr. Cirurgião", "33333333333", "s@teste.com", "31933333333", "CRM3");
        cirurgiao.setId(3L);
    }

    private Consulta rotina(Profissional profissional, LocalDate data) {
        return new ConsultaRotina(paciente, profissional, data, LocalTime.of(9, 0), "Revisão", null);
    }

    private Consulta especializada(Profissional profissional, LocalDate data) {
        return new ConsultaEspecializada(paciente, profissional, data, LocalTime.of(10, 0),
                "Coração", null, Especialidade.CARDIOLOGIA);
    }

    /** Cirurgia de mentira: só precisa de profissional, data e situação. */
    private Atendimento cirurgia(Profissional profissional, String situacao) {
        return new Atendimento() {
            @Override public Paciente getPaciente() { return paciente; }
            @Override public Profissional getProfissional() { return profissional; }
            @Override public LocalDateTime getDataHoraPrevista() { return DIA.atTime(8, 0); }
            @Override public String getSituacao() { return situacao; }
            @Override public void validarAtendimento() { }
        };
    }

    // ---------- por modalidade ----------

    @Test
    void contaAtendimentosPorModalidadeIncluindoAsSemAtendimento() {
        List<Atendimento> atendimentos = List.of(
                rotina(clinico, DIA), rotina(clinico, DIA.plusDays(1)),
                especializada(cardiologista, DIA),
                cirurgia(cirurgiao, "AGENDADO"));

        List<AtendimentosPorModalidadeResponse> resultado = IndicadoresAtendimento.porModalidade(
                atendimentos, IndicadoresAtendimento.filtro(null, null, false));

        assertEquals(List.of("ROTINA", "ESPECIALIZADA", "EMERGENCIA", "CIRURGIA"),
                resultado.stream().map(AtendimentosPorModalidadeResponse::modalidade).toList());
        assertEquals(List.of(2L, 1L, 0L, 1L),
                resultado.stream().map(AtendimentosPorModalidadeResponse::total).toList());
    }

    // ---------- por profissional ----------

    @Test
    void contaAtendimentosPorProfissionalComDetalheEOrdenaPeloTotal() {
        List<Atendimento> atendimentos = List.of(
                rotina(clinico, DIA), rotina(clinico, DIA.plusDays(1)), especializada(cardiologista, DIA),
                cirurgia(cirurgiao, "AGENDADO"), cirurgia(cirurgiao, "CONCLUIDO"), cirurgia(cirurgiao, "AGENDADO"));

        List<AtendimentosPorProfissionalResponse> resultado = IndicadoresAtendimento.porProfissional(
                atendimentos, IndicadoresAtendimento.filtro(null, null, false));

        assertEquals(3, resultado.size());

        // Cirurgião (3) vem antes do clínico (2) e do especialista (1).
        assertEquals(List.of(3L, 1L, 2L), resultado.stream()
                .map(AtendimentosPorProfissionalResponse::profissionalId).toList());

        var doClinico = resultado.get(1);
        assertEquals(2L, doClinico.total());
        assertEquals("CLINICO", doClinico.tipo());
        assertEquals(Map.of("ROTINA", 2L, "ESPECIALIZADA", 0L, "EMERGENCIA", 0L, "CIRURGIA", 0L),
                doClinico.porModalidade());
    }

    @Test
    void semAtendimentosDevolveListaVazia() {
        assertTrue(IndicadoresAtendimento.porProfissional(
                List.of(), IndicadoresAtendimento.filtro(null, null, false)).isEmpty());
    }

    @Test
    void semAtendimentosDoProfissionalVeioZerado() {
        var zerado = IndicadoresAtendimento.semAtendimentos(clinico);

        assertEquals(0L, zerado.total());
        assertEquals(4, zerado.porModalidade().size());
        assertTrue(zerado.porModalidade().values().stream().allMatch(v -> v == 0L));
    }

    // ---------- predicados ----------

    @Test
    void canceladosEFaltasNaoEntramNaContagem() {
        Consulta cancelada = rotina(clinico, DIA);
        cancelada.cancelar();
        Consulta falta = rotina(clinico, DIA);
        falta.registrarNaoComparecimento();
        Consulta valida = rotina(clinico, DIA);

        var resultado = IndicadoresAtendimento.porProfissional(
                List.of(cancelada, falta, valida, cirurgia(cirurgiao, "CANCELADO")),
                IndicadoresAtendimento.filtro(null, null, false));

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).total());
    }

    @Test
    void apenasConcluidosContaSoOsConcluidos() {
        Consulta emAtendimento = rotina(clinico, DIA);
        emAtendimento.iniciar();
        Consulta concluida = rotina(clinico, DIA);
        concluida.iniciar();
        concluida.concluir("ok");

        var resultado = IndicadoresAtendimento.porModalidade(
                List.of(emAtendimento, concluida, cirurgia(cirurgiao, "CONCLUIDO"), cirurgia(cirurgiao, "AGENDADO")),
                IndicadoresAtendimento.filtro(null, null, true));

        assertEquals(1L, resultado.get(0).total()); // ROTINA
        assertEquals(1L, resultado.get(3).total()); // CIRURGIA
    }

    @Test
    void periodoLimitaOsAtendimentosContados() {
        List<Atendimento> atendimentos = List.of(
                rotina(clinico, DIA.minusDays(5)), rotina(clinico, DIA), rotina(clinico, DIA.plusDays(5)));

        var soOMeio = IndicadoresAtendimento.porModalidade(atendimentos,
                IndicadoresAtendimento.filtro(DIA.minusDays(1), DIA.plusDays(1), false));
        var aPartirDe = IndicadoresAtendimento.porModalidade(atendimentos,
                IndicadoresAtendimento.filtro(DIA, null, false));
        var ate = IndicadoresAtendimento.porModalidade(atendimentos,
                IndicadoresAtendimento.filtro(null, DIA, false));

        assertEquals(1L, soOMeio.get(0).total());
        assertEquals(2L, aPartirDe.get(0).total());
        assertEquals(2L, ate.get(0).total());
    }

    @Test
    void modalidadeDeCadaTipoDeAtendimento() {
        assertEquals("ROTINA", IndicadoresAtendimento.MODALIDADE.apply(rotina(clinico, DIA)));
        assertEquals("ESPECIALIZADA", IndicadoresAtendimento.MODALIDADE.apply(especializada(cardiologista, DIA)));
        assertEquals("CIRURGIA", IndicadoresAtendimento.MODALIDADE.apply(cirurgia(cirurgiao, "AGENDADO")));
    }
}
