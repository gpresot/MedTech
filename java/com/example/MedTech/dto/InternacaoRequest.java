package com.example.MedTech.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record InternacaoRequest(
        @NotNull(message = "O paciente é obrigatório.") Long pacienteId,
        @NotNull(message = "O profissional responsável é obrigatório.") Long profissionalId,
        @NotNull(message = "O quarto é obrigatório.") Long quartoId,
        LocalDate dataEntrada,
        @NotNull(message = "A data prevista de alta é obrigatória.") LocalDate dataPrevistaAlta,
        String observacoes) {
}
