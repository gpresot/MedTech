package com.example.MedTech.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.enums.PrioridadeAtendimento;
import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@DiscriminatorValue("EMERGENCIA")
public class AtendimentoEmergencia extends Consulta {

    @Enumerated(EnumType.STRING)
    private PrioridadeAtendimento prioridade;

    public AtendimentoEmergencia() {
    }

    public AtendimentoEmergencia(Paciente paciente, Profissional profissional, LocalDate data,
            LocalTime horario, String motivo, String observacoesMedicas,
            PrioridadeAtendimento prioridade) {
        super(paciente, profissional, data, horario, motivo, observacoesMedicas);
        this.prioridade = prioridade;
    }

    @Override
    public void validarAtendimento() {
        super.validarAtendimento();
        if (prioridade == null) {
            throw new RegraNegocioException("Informe a prioridade do atendimento de emergência.");
        }
    }

    public PrioridadeAtendimento getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeAtendimento prioridade) {
        this.prioridade = prioridade;
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.EMERGENCIA;
    }
}
