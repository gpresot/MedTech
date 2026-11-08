package com.example.MedTech.dto;

import jakarta.validation.constraints.Min;

public record QuartoRequest(
        @Min(value = 1, message = "O número do quarto deve ser positivo.") int numero,
        @Min(value = 0, message = "O andar não pode ser negativo.") int andar,
        @Min(value = 1, message = "A capacidade máxima deve ser de pelo menos 1.") int capacidadeMaxima) {
}
