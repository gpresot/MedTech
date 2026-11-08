package com.example.MedTech.dto;

import java.time.LocalDateTime;

import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.model.ProcedimentoCirurgico;

public record ProcedimentoResponse(
        Long id,
        Long internacaoId,
        Long pacienteId,
        String pacienteNome,
        Long cirurgiaoId,
        String cirurgiaoNome,
        String descricao,
        LocalDateTime dataHora,
        StatusProcedimento status,
        String observacoesPreOperatorias,
        String observacoesPosOperatorias) {

    public static ProcedimentoResponse de(ProcedimentoCirurgico p) {
        var paciente = p.getInternacao().getPaciente();
        return new ProcedimentoResponse(
                p.getId(), p.getInternacao().getId(),
                paciente.getId(), paciente.getNome(),
                p.getCirurgiao().getId(), p.getCirurgiao().getNome(),
                p.getDescricao(), p.getDataHora(), p.getStatus(),
                p.getObservacoesPreOperatorias(), p.getObservacoesPosOperatorias());
    }
}
