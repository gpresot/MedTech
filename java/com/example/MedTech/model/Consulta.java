package com.example.MedTech.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.*;

@Entity
@Table(name = "consultas", indexes = {
    @Index(name = "idx_consultas_paciente", columnList = "paciente_id"),
    @Index(name = "idx_consultas_agenda", columnList = "profissional_id,data,horario")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_consulta")
public class Consulta extends Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String observacoesMedicas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    public Consulta() {
    }

    public Consulta(Paciente paciente, Profissional profissional, LocalDate data,
            LocalTime horario, String motivo, String observacoesMedicas) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.data = data;
        this.horario = horario;
        this.motivo = motivo;
        this.observacoesMedicas = observacoesMedicas;
    }

    public TipoConsulta getTipo() {
        return TipoConsulta.ROTINA;
    }

    public void remarcar(LocalDate data, LocalTime horario) {
        verificarAgendada();
        this.data = data;
        this.horario = horario;
    }

    public void iniciar() {
        verificarAguardando();
        validarAtendimento();
        status = StatusConsulta.EM_ATENDIMENTO;
    }

    public void confirmar() {
        verificarAgendada();
        status = StatusConsulta.CONFIRMADA;
    }

    public void registrarNaoComparecimento() {
        verificarAguardando();
        status = StatusConsulta.NAO_COMPARECEU;
    }

    public void cancelar() {
        if (status == StatusConsulta.CONCLUIDA || status == StatusConsulta.CANCELADA
                || status == StatusConsulta.NAO_COMPARECEU) {
            throw new RegraNegocioException(
                    "Não é possível cancelar uma consulta encerrada.");
        }
        status = StatusConsulta.CANCELADA;
    }

    public void concluir(String observacoes) {
        if (status != StatusConsulta.EM_ATENDIMENTO) {
            throw new RegraNegocioException(
                    "Apenas consultas em atendimento podem ser concluídas.");
        }
        observacoesMedicas = observacoes;
        status = StatusConsulta.CONCLUIDA;
    }

    @Override
    public void validarAtendimento() {
        validarDadosComuns();
        if (data == null || horario == null || motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Preencha os dados obrigatórios da consulta.");
        }
    }

    private void verificarAguardando() {
        if (status != StatusConsulta.AGENDADA && status != StatusConsulta.CONFIRMADA) {
            throw new RegraNegocioException("A consulta precisa estar aguardando atendimento.");
        }
    }

    public void verificarAgendada() {
        if (status != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("A consulta precisa estar agendada para esta operação.");
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    @Override
    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacoesMedicas() {
        return observacoesMedicas;
    }

    public void setObservacoesMedicas(String observacoesMedicas) {
        this.observacoesMedicas = observacoesMedicas;
    }

    public StatusConsulta getStatus() {
        return status;
    }
}
