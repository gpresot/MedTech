package com.example.MedTech.model;

import java.time.LocalDateTime;

import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "procedimentos_cirurgicos")
public class ProcedimentoCirurgico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", nullable = false)
    private Internacao internacao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cirurgiao_id", nullable = false)
    private Cirurgiao cirurgiao;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcedimento status = StatusProcedimento.AGENDADO;

    @Column(columnDefinition = "TEXT")
    private String observacoesPreOperatorias;

    @Column(columnDefinition = "TEXT")
    private String observacoesPosOperatorias;

    public ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(Internacao internacao, Cirurgiao cirurgiao,
            String descricao, LocalDateTime dataHora, String observacoesPreOperatorias) {
        this.internacao = internacao;
        this.cirurgiao = cirurgiao;
        this.descricao = descricao;
        this.dataHora = dataHora;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }

    public boolean estaAtivo() {
        return status == StatusProcedimento.AGENDADO
                || status == StatusProcedimento.EM_ANDAMENTO;
    }

    public void registrarObservacoesPreOperatorias(String observacoes) {
        if (status != StatusProcedimento.AGENDADO) {
            throw new RegraNegocioException(
                    "Observações pré-operatórias só podem ser alteradas antes do início da cirurgia.");
        }
        this.observacoesPreOperatorias = observacoes;
    }

    public void iniciar() {
        if (status != StatusProcedimento.AGENDADO) {
            throw new RegraNegocioException(
                    "Apenas cirurgias agendadas podem ser iniciadas.");
        }
        if (observacoesPreOperatorias == null || observacoesPreOperatorias.isBlank()) {
            throw new RegraNegocioException(
                    "Registre as observações pré-operatórias antes de iniciar a cirurgia.");
        }
        this.status = StatusProcedimento.EM_ANDAMENTO;
    }

    public void concluir(String observacoesPos) {
        if (status != StatusProcedimento.EM_ANDAMENTO) {
            throw new RegraNegocioException(
                    "Apenas cirurgias em andamento podem ser concluídas.");
        }
        this.observacoesPosOperatorias = observacoesPos;
        this.status = StatusProcedimento.CONCLUIDO;
    }

    public void cancelar() {
        if (status == StatusProcedimento.CONCLUIDO
                || status == StatusProcedimento.CANCELADO) {
            throw new RegraNegocioException(
                    "Cirurgias concluídas ou já canceladas não podem ser canceladas.");
        }
        this.status = StatusProcedimento.CANCELADO;
    }

    public Long getId() { return id; }
    public Internacao getInternacao() { return internacao; }
    public Cirurgiao getCirurgiao() { return cirurgiao; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusProcedimento getStatus() { return status; }
    public String getObservacoesPreOperatorias() { return observacoesPreOperatorias; }
    public String getObservacoesPosOperatorias() { return observacoesPosOperatorias; }
}
