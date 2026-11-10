package com.example.MedTech.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.StatusAtendimento;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.PrioridadeAtendimento;
import com.example.MedTech.model.Consulta;
import com.example.MedTech.model.ConsultaEspecializada;
import com.example.MedTech.model.AtendimentoEmergencia;

public record ConsultaResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        LocalDate data,
        LocalTime horario,
        String motivo,
        String observacoesMedicas,
        StatusAtendimento status,
        TipoConsulta tipo,
        Especialidade especialidade,
        PrioridadeAtendimento prioridade) {

    public static ConsultaResponse de(Consulta consulta) {
        Especialidade especialidade = null;
        PrioridadeAtendimento prioridade = null;
        if (consulta instanceof ConsultaEspecializada) {
            especialidade = ((ConsultaEspecializada) consulta).getEspecialidade();
        }
        if (consulta instanceof AtendimentoEmergencia) {
            prioridade = ((AtendimentoEmergencia) consulta).getPrioridade();
        }
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPaciente().getId(), consulta.getPaciente().getNome(),
                consulta.getProfissional().getId(), consulta.getProfissional().getNome(),
                consulta.getData(), consulta.getHorario(), consulta.getMotivo(),
                consulta.getObservacoesMedicas(), consulta.getStatus(), consulta.getTipo(),
                especialidade, prioridade);
    }
}
