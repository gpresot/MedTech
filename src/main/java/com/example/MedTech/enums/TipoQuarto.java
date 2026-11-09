package com.example.MedTech.enums;

/** Categoria do quarto e limite máximo de leitos permitido. */
public enum TipoQuarto {
    ENFERMARIA(6),
    APARTAMENTO(2),
    UTI(1);

    private final int capacidadeMaxima;

    TipoQuarto(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
