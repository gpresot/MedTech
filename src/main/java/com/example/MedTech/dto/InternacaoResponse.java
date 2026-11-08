package com.example.MedTech.dto;

import java.time.LocalDate;

import com.example.MedTech.model.Internacao;

public record InternacaoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        Long quartoId,
        int quartoNumero,
        LocalDate dataEntrada,
        LocalDate dataPrevistaAlta,
        LocalDate dataEfetivaAlta,
        boolean ativa,
        String observacoes) {

    public static InternacaoResponse de(Internacao i) {
        return new InternacaoResponse(
                i.getId(),
                i.getPaciente().getId(), i.getPaciente().getNome(),
                i.getProfissional().getId(), i.getProfissional().getNome(),
                i.getQuarto().getId(), i.getQuarto().getNumero(),
                i.getDataEntrada(), i.getDataPrevistaAlta(), i.getDataEfetivaAlta(),
                i.estaAtiva(), i.getObservacoes());
    }
}
