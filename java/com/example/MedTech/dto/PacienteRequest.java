package com.example.MedTech.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public record PacienteRequest(
        @NotBlank(message = "O nome é obrigatório.") String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 números.")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,

        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 números.")
        String telefone,

        @NotBlank(message = "O endereço é obrigatório.") String endereco,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email) {
}
