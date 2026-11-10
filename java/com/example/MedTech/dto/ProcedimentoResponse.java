package com.example.MedTech.dto;

import java.time.LocalDateTime;

import com.example.MedTech.enums.StatusAtendimento;
import com.example.MedTech.enums.TipoCirurgia;
import com.example.MedTech.model.ProcedimentoCirurgico;

public record ProcedimentoResponse(
        Long id,
        Long internacaoId,
        Long pacienteId,
        String pacienteNome,
        Long cirurgiaoId,
        String cirurgiaoNome,
        String descricao,
        TipoCirurgia tipo,
        LocalDateTime dataHora,
        LocalDateTime dataHoraInicioEfetivo,
        LocalDateTime dataHoraFimEfetivo,
        StatusAtendimento status,
        String observacoesPreOperatorias,
        String observacoesPosOperatorias) {

    public static ProcedimentoResponse de(ProcedimentoCirurgico p) {
        var paciente = p.getPaciente();
        return new ProcedimentoResponse(
                p.getId(), p.getInternacao().getId(),
                paciente.getId(), paciente.getNome(),
                p.getCirurgiao().getId(), p.getCirurgiao().getNome(),
                p.getDescricao(), p.getTipo(), p.getDataHora(),
                p.getDataHoraInicioEfetivo(), p.getDataHoraFimEfetivo(), p.getStatus(),
                p.getObservacoesPreOperatorias(), p.getObservacoesPosOperatorias());
    }
}
