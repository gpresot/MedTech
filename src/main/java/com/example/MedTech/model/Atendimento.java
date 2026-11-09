package com.example.MedTech.model;

import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.MappedSuperclass;

/**
 * Base abstrata das modalidades de atendimento.
 * Define o contrato polimórfico para paciente, profissional responsável
 * e validação, sem obrigar modalidades diferentes a armazenarem datas/status
 * com formatos idênticos.
 */
@MappedSuperclass
public abstract class Atendimento {

    public abstract Paciente getPaciente();

    public abstract Profissional getProfissional();

    public abstract void validarAtendimento();

    protected void validarDadosComuns() {
        if (getPaciente() == null || getProfissional() == null) {
            throw new RegraNegocioException(
                    "Todo atendimento deve estar associado a um paciente e a um profissional responsável.");
        }
    }
}
