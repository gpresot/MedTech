package com.example.MedTech.model;

import com.example.MedTech.enums.StatusQuarto;
import com.example.MedTech.enums.TipoQuarto;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'ENFERMARIA'")
    private TipoQuarto tipo = TipoQuarto.ENFERMARIA;

    @Column(nullable = false)
    private int capacidadeMaxima;

    @Column(nullable = false)
    private int ocupacaoAtual = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusQuarto status = StatusQuarto.DISPONIVEL;

    public Quarto() {
    }

    /** Construtor legado: quartos antigos passam a ser enfermarias. */
    public Quarto(int numero, int andar, int capacidadeMaxima) {
        this(numero, andar, TipoQuarto.ENFERMARIA, capacidadeMaxima);
    }

    public Quarto(int numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        this.numero = numero;
        this.andar = andar;
        this.tipo = tipo != null ? tipo : TipoQuarto.ENFERMARIA;
        validarCapacidade(capacidadeMaxima, this.tipo, 0);
        this.capacidadeMaxima = capacidadeMaxima;
    }

    private void validarCapacidade(int capacidade, TipoQuarto categoria, int ocupacao) {
        if (categoria == null) {
            throw new RegraNegocioException("Informe o tipo do quarto.");
        }
        if (capacidade < 1 || capacidade > categoria.getCapacidadeMaxima()) {
            throw new RegraNegocioException("A capacidade do tipo " + categoria
                    + " deve ser entre 1 e " + categoria.getCapacidadeMaxima() + " leito(s).");
        }
        if (capacidade < ocupacao) {
            throw new RegraNegocioException(
                    "A capacidade não pode ser menor que a ocupação atual (" + ocupacao + ").");
        }
    }

    public boolean verificarDisponibilidade() {
        return status != StatusQuarto.MANUTENCAO && ocupacaoAtual < capacidadeMaxima;
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

    public void atualizarStatus() {
        if (status == StatusQuarto.MANUTENCAO) return;
        status = ocupacaoAtual >= capacidadeMaxima
                ? StatusQuarto.OCUPADO : StatusQuarto.DISPONIVEL;
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
        return status == StatusQuarto.MANUTENCAO ? 0 : capacidadeMaxima - ocupacaoAtual;
    }

    /** Construtor de atualização legado: preserva o tipo atual do quarto. */
    public void atualizarDados(int numero, int andar, int capacidadeMaxima) {
        atualizarDados(numero, andar, this.tipo, capacidadeMaxima);
    }

    public void atualizarDados(int numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        TipoQuarto novoTipo = tipo != null ? tipo : this.tipo;
        validarCapacidade(capacidadeMaxima, novoTipo, ocupacaoAtual);
        this.numero = numero;
        this.andar = andar;
        this.tipo = novoTipo;
        this.capacidadeMaxima = capacidadeMaxima;
        atualizarStatus();
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public int getAndar() { return andar; }
    public TipoQuarto getTipo() { return tipo; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public int getOcupacaoAtual() { return ocupacaoAtual; }
    public StatusQuarto getStatus() { return status; }
}
