package com.example.MedTech.util;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.example.MedTech.dto.ConsultaResponse;
import com.example.MedTech.enums.StatusConsulta;

/**
 * Separa as consultas para as páginas de perfil: as em aberto (da mais próxima
 * para a mais distante) e as encerradas (da mais recente para a mais antiga).
 */
public record AgendaConsultas(
        List<ConsultaResponse> proximas,
        List<ConsultaResponse> anteriores) {

    private static final Set<StatusConsulta> EM_ABERTO = Set.of(
            StatusConsulta.AGENDADA,
            StatusConsulta.CONFIRMADA,
            StatusConsulta.EM_ATENDIMENTO);

    public static AgendaConsultas de(List<ConsultaResponse> consultas) {
        List<ConsultaResponse> proximas = consultas.stream()
                .filter(c -> EM_ABERTO.contains(c.status()))
                .sorted(Comparator.comparing(ConsultaResponse::data)
                        .thenComparing(ConsultaResponse::horario))
                .toList();

        List<ConsultaResponse> anteriores = consultas.stream()
                .filter(c -> !EM_ABERTO.contains(c.status()))
                .sorted(Comparator.comparing(ConsultaResponse::data)
                        .thenComparing(ConsultaResponse::horario)
                        .reversed())
                .toList();

        return new AgendaConsultas(proximas, anteriores);
    }
}
