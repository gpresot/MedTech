package com.example.MedTech.dto;

import jakarta.validation.constraints.NotBlank;

public record InformacaoRequest(
        @NotBlank(message = "A informação é obrigatória.") String informacao) {
}
