package com.example.MedTech.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcedimentoRequest(
        @NotNull(message = "A internação é obrigatória.") Long internacaoId,
        @NotNull(message = "O cirurgião é obrigatório.") Long cirurgiaoId,
        @NotBlank(message = "A descrição é obrigatória.") String descricao,
        @NotNull(message = "A data e hora são obrigatórias.") LocalDateTime dataHora,
        String observacoesPreOperatorias) {
}
