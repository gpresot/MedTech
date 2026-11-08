package com.example.MedTech.model;

import com.example.MedTech.enums.StatusQuarto;
import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quartos")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int andar;

    @Column(nullable = false)
    private int capacidadeMaxima;

    @Column(nullable = false)
    private int ocupacaoAtual = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusQuarto status = StatusQuarto.DISPONIVEL;

    public Quarto() {
    }

    public Quarto(int numero, int andar, int capacidadeMaxima) {
        this.numero = numero;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public boolean verificarDisponibilidade() {
        return status != StatusQuarto.MANUTENCAO
                && ocupacaoAtual < capacidadeMaxima;
    }

    public void adicionarPaciente() {
        if (!verificarDisponibilidade()) {
            throw new RegraNegocioException(
                    "O quarto " + numero + " não possui vaga disponível.");
        }
        ocupacaoAtual++;
        atualizarStatus();
    }

    public void removerPaciente() {
        if (ocupacaoAtual == 0) {
            throw new RegraNegocioException(
                    "O quarto " + numero + " não possui pacientes internados.");
        }
        ocupacaoAtual--;
        atualizarStatus();
    }

    /** Recalcula DISPONIVEL/OCUPADO conforme a lotação. Manutenção é preservada. */
    public void atualizarStatus() {
        if (status == StatusQuarto.MANUTENCAO) {
            return;
        }
        status = ocupacaoAtual >= capacidadeMaxima
                ? StatusQuarto.OCUPADO
                : StatusQuarto.DISPONIVEL;
    }

    public void colocarEmManutencao() {
        if (ocupacaoAtual > 0) {
            throw new RegraNegocioException(
                    "Não é possível colocar em manutenção um quarto com pacientes internados.");
        }
        status = StatusQuarto.MANUTENCAO;
    }

    public void sairDeManutencao() {
        status = StatusQuarto.DISPONIVEL;
        atualizarStatus();
    }

    public int vagasDisponiveis() {
        return status == StatusQuarto.MANUTENCAO
                ? 0
                : capacidadeMaxima - ocupacaoAtual;
    }

    public void atualizarDados(int numero, int andar, int capacidadeMaxima) {
        if (capacidadeMaxima < ocupacaoAtual) {
            throw new RegraNegocioException(
                    "A capacidade não pode ser menor que a ocupação atual (" + ocupacaoAtual + ").");
        }
        this.numero = numero;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        atualizarStatus();
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public int getAndar() { return andar; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public int getOcupacaoAtual() { return ocupacaoAtual; }
    public StatusQuarto getStatus() { return status; }
}
