package com.example.MedTech.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.PrioridadeAtendimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConsultaRequest(
        @NotNull(message = "O paciente é obrigatório.") @Positive Long pacienteId,
        @NotNull(message = "O profissional é obrigatório.") @Positive Long profissionalId,
        @NotNull(message = "A data é obrigatória.") LocalDate data,
        @NotNull(message = "O horário é obrigatório.") LocalTime horario,
        @NotBlank(message = "O motivo é obrigatório.") String motivo,
        String observacoesMedicas,
        @NotNull(message = "O tipo da consulta é obrigatório.") TipoConsulta tipo,
        Especialidade especialidade,
        PrioridadeAtendimento prioridade) {
}
