package com.example.MedTech.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.exception.RegraNegocioException;

import org.hibernate.Hibernate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("ROTINA")
public class ConsultaRotina extends Consulta {

    /** Especialidade para a qual o clínico geral encaminhou o paciente (opcional). */
    @Enumerated(EnumType.STRING)
    private Especialidade especialidadeEncaminhada;

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

    /**
     * Encaminha o paciente para atendimento especializado. Só o clínico geral
     * responsável pode encaminhar, durante ou depois do atendimento.
     */
    public void encaminhar(Especialidade especialidade) {
        if (especialidade == null) {
            throw new RegraNegocioException("Informe a especialidade do encaminhamento.");
        }
        if (getStatus() != StatusConsulta.EM_ATENDIMENTO && getStatus() != StatusConsulta.CONCLUIDA) {
            throw new RegraNegocioException(
                    "O encaminhamento só pode ser feito durante ou após o atendimento.");
        }
        this.especialidadeEncaminhada = especialidade;
    }

    public Especialidade getEspecialidadeEncaminhada() {
        return especialidadeEncaminhada;
    }
}
