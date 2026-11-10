package com.example.MedTech.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.exception.RegraNegocioException;

import org.hibernate.Hibernate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@DiscriminatorValue("ESPECIALIZADA")
public class ConsultaEspecializada extends Consulta {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    public ConsultaEspecializada() {
    }

    public ConsultaEspecializada(Paciente paciente, Profissional profissional, LocalDate data,
            LocalTime horario, String motivo, String observacoesMedicas, Especialidade especialidade) {
        super(paciente, profissional, data, horario, motivo, observacoesMedicas);
        this.especialidade = especialidade;
    }

    @Override
    public void validarAtendimento() {
        super.validarAtendimento();
        if (especialidade == null) {
            throw new RegraNegocioException("Informe a especialidade da consulta.");
        }
        Object profissional = Hibernate.unproxy(getProfissional());
        if (!(profissional instanceof MedicoEspecialista)) {
            throw new RegraNegocioException("A consulta especializada exige um médico especialista.");
        }
        MedicoEspecialista especialista = (MedicoEspecialista) profissional;
        if (especialista.getEspecialidade() != especialidade) {
            throw new RegraNegocioException("A especialidade do médico não corresponde à consulta.");
        }
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ESPECIALIZADA;
    }
}
