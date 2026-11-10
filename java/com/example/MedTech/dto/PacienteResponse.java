package com.example.MedTech.dto;

import java.time.LocalDate;

import com.example.MedTech.model.Paciente;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String telefone,
        String endereco,
        String email) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(),
                p.getDataNascimento(), p.getTelefone(), p.getEndereco(), p.getEmail());
    }
}
