package com.example.MedTech.dto;

import com.example.MedTech.enums.StatusQuarto;
import com.example.MedTech.model.Quarto;

public record QuartoResponse(
        Long id,
        int numero,
        int andar,
        int capacidadeMaxima,
        int ocupacaoAtual,
        int vagasDisponiveis,
        StatusQuarto status) {

    public static QuartoResponse de(Quarto q) {
        return new QuartoResponse(q.getId(), q.getNumero(), q.getAndar(),
                q.getCapacidadeMaxima(), q.getOcupacaoAtual(),
                q.vagasDisponiveis(), q.getStatus());
    }
}
