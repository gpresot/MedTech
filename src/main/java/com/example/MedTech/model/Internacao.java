package com.example.MedTech.model;

import java.time.LocalDate;

import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "internacoes")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;

    @Column(nullable = false)
    private LocalDate dataEntrada;

    private LocalDate dataPrevistaAlta;

    private LocalDate dataEfetivaAlta;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public Internacao() {
    }

    public Internacao(Paciente paciente, Profissional profissional, Quarto quarto,
            LocalDate dataEntrada, LocalDate dataPrevistaAlta, String observacoes) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.quarto = quarto;
        this.dataEntrada = dataEntrada;
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
    }

    public boolean estaAtiva() {
        return dataEfetivaAlta == null;
    }

    public void registrarAlta(LocalDate dataAlta) {
        if (!estaAtiva()) {
            throw new RegraNegocioException("Esta internação já recebeu alta.");
        }
        if (dataAlta.isBefore(dataEntrada)) {
            throw new RegraNegocioException(
                    "A data de alta não pode ser anterior à data de entrada.");
        }
        this.dataEfetivaAlta = dataAlta;
    }

    public void alterarPrevisaoAlta(LocalDate data) {
        if (!estaAtiva()) {
            throw new RegraNegocioException(
                    "Não é possível alterar a previsão de uma internação encerrada.");
        }
        if (data.isBefore(dataEntrada)) {
            throw new RegraNegocioException(
                    "A previsão de alta não pode ser anterior à data de entrada.");
        }
        this.dataPrevistaAlta = data;
    }

    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public Profissional getProfissional() { return profissional; }
    public Quarto getQuarto() { return quarto; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataPrevistaAlta() { return dataPrevistaAlta; }
    public LocalDate getDataEfetivaAlta() { return dataEfetivaAlta; }
    public String getObservacoes() { return observacoes; }
}
