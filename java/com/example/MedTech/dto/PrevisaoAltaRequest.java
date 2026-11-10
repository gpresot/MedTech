package com.example.MedTech.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PrevisaoAltaRequest(
        @NotNull(message = "A data prevista de alta é obrigatória.") LocalDate dataPrevistaAlta) {
}
