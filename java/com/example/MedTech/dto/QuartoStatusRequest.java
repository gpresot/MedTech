package com.example.MedTech.dto;

import com.example.MedTech.enums.StatusQuarto;

import jakarta.validation.constraints.NotNull;

/** Só aceita MANUTENCAO ou DISPONIVEL; OCUPADO é calculado pela lotação. */
public record QuartoStatusRequest(
        @NotNull(message = "O status é obrigatório.") StatusQuarto status) {
}
