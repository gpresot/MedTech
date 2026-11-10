package com.example.MedTech.dto;

import java.util.List;

public record HistoricoMedicoResponse(
        Long pacienteId,
        String pacienteNome,
        String informacoesRelevantes,
        List<ConsultaResponse> consultas,
        List<InternacaoResponse> internacoes,
        List<ProcedimentoResponse> procedimentos) {
}
