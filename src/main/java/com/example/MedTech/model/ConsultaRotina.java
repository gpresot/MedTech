package com.example.MedTech.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.exception.RegraNegocioException;

import org.hibernate.Hibernate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ROTINA")
public class ConsultaRotina extends Consulta {

    public ConsultaRotina() {
    }

    public ConsultaRotina(Paciente paciente, Profissional profissional, LocalDate data,
            LocalTime horario, String motivo, String observacoesMedicas) {
        super(paciente, profissional, data, horario, motivo, observacoesMedicas);
    }

    @Override
    public void validarAtendimento() {
        super.validarAtendimento();
        if (!(Hibernate.unproxy(getProfissional()) instanceof MedicoClinicoGeral)) {
            throw new RegraNegocioException("Consultas de rotina exigem um médico clínico geral.");
        }
    }

    @Override
    public TipoConsulta getTipo() {
        return TipoConsulta.ROTINA;
    }
}
