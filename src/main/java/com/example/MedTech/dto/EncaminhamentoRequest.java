package com.example.MedTech.dto;

import com.example.MedTech.enums.Especialidade;

import jakarta.validation.constraints.NotNull;

public record EncaminhamentoRequest(
        @NotNull(message = "A especialidade é obrigatória.") Especialidade especialidade) {
}
