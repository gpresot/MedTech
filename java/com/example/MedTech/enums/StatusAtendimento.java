package com.example.MedTech.enums;

/**
 * Status único para qualquer atendimento (consulta, atendimento de emergência
 * ou procedimento cirúrgico). Substitui os antigos StatusConsulta e
 * StatusProcedimento, que tinham o mesmo ciclo de vida.
 */
public enum StatusAtendimento {
    AGENDADO,
    CONFIRMADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO,
    NAO_COMPARECEU
}
