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

    /** 999.999.999-99; se o CPF não tiver 11 dígitos, devolve como está. */
    public String cpfFormatado() {
        if (cpf == null || cpf.length() != 11) {
            return cpf == null ? "—" : cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(),
                p.getDataNascimento(), p.getTelefone(), p.getEndereco(), p.getEmail());
    }
}
