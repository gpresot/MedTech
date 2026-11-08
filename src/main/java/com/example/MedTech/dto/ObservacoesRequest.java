package com.example.MedTech.dto;

import jakarta.validation.constraints.NotBlank;

public record ObservacoesRequest(
        @NotBlank(message = "As observações são obrigatórias.") String observacoes) {
}
