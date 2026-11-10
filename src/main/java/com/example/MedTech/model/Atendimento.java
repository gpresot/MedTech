package com.example.MedTech.model;

import java.time.LocalDateTime;

import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Base abstrata das modalidades de atendimento (consultas e procedimentos
 * cirúrgicos). Concentra o que é comum — identificador, paciente, profissional
 * responsável, data/hora prevista e a validação básica — e deixa para cada
 * modalidade a sua própria validação ({@link #validarAtendimento()}).
 * Não cria tabela própria (@MappedSuperclass).
 */
@MappedSuperclass
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public abstract Paciente getPaciente();

    public abstract Profissional getProfissional();

    /** Data e hora previstas, em formato único para todas as modalidades. */
    public abstract LocalDateTime getDataHoraPrevista();

    /** Situação atual como texto (nome da constante do enum da modalidade). */
    public abstract String getSituacao();

    /** Condições específicas que cada modalidade exige para ser realizada. */
    public abstract void validarAtendimento();

    protected void validarDadosComuns() {
        if (getPaciente() == null || getProfissional() == null) {
            throw new RegraNegocioException(
                    "Todo atendimento deve estar associado a um paciente e a um profissional responsável.");
        }
    }
}
