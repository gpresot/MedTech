package com.example.MedTech.dto;

import java.util.Map;

/**
 * Quantidade de atendimentos de um profissional, no total e por modalidade
 * (ROTINA, ESPECIALIZADA, EMERGENCIA e CIRURGIA; as sem atendimento vêm com 0).
 */
public record AtendimentosPorProfissionalResponse(
        Long profissionalId,
        String nome,
        String tipo,
        long total,
        Map<String, Long> porModalidade) {
}
