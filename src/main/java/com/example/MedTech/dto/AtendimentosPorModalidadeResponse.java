package com.example.MedTech.dto;

/** Quantidade de atendimentos de uma modalidade (consulta de rotina, cirurgia...). */
public record AtendimentosPorModalidadeResponse(String modalidade, long total) {
}
