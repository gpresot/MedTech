package com.example.MedTech.model;

import java.time.LocalDateTime;

import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.enums.TipoCirurgia;
import com.example.MedTech.exception.RegraNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "procedimentos_cirurgicos")
public class ProcedimentoCirurgico extends Atendimento {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", nullable = false)
    private Internacao internacao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cirurgiao_id", nullable = false)
    private Cirurgiao cirurgiao;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'ELETIVA'")
    private TipoCirurgia tipo = TipoCirurgia.ELETIVA;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private LocalDateTime dataHoraInicioEfetivo;
    private LocalDateTime dataHoraFimEfetivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcedimento status = StatusProcedimento.AGENDADO;

    @Column(columnDefinition = "TEXT")
    private String observacoesPreOperatorias;

    @Column(columnDefinition = "TEXT")
    private String observacoesPosOperatorias;

    public ProcedimentoCirurgico() {
    }

    /** Construtor legado: procedimentos existentes são classificados como eletivos. */
    public ProcedimentoCirurgico(Internacao internacao, Cirurgiao cirurgiao,
            String descricao, LocalDateTime dataHora, String observacoesPreOperatorias) {
        this(internacao, cirurgiao, descricao, TipoCirurgia.ELETIVA,
                dataHora, observacoesPreOperatorias);
    }

    public ProcedimentoCirurgico(Internacao internacao, Cirurgiao cirurgiao,
            String descricao, TipoCirurgia tipo, LocalDateTime dataHora,
            String observacoesPreOperatorias) {
        this.internacao = internacao;
        this.cirurgiao = cirurgiao;
        this.descricao = descricao;
        this.tipo = tipo != null ? tipo : TipoCirurgia.ELETIVA;
        this.dataHora = dataHora;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        validarAtendimento();
    }

    @Override
    public void validarAtendimento() {
        validarDadosComuns();
        if (internacao == null || descricao == null || descricao.isBlank() || dataHora == null) {
            throw new RegraNegocioException(
                    "Informe a internação, o cirurgião, a descrição e a data e hora previstas da cirurgia.");
        }
        if (tipo == null) {
            throw new RegraNegocioException("Informe o tipo da cirurgia.");
        }
    }

    public boolean estaAtivo() {
        return status == StatusProcedimento.AGENDADO || status == StatusProcedimento.EM_ANDAMENTO;
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
            throw new RegraNegocioException("Apenas cirurgias agendadas podem ser iniciadas.");
        }
        if (observacoesPreOperatorias == null || observacoesPreOperatorias.isBlank()) {
            throw new RegraNegocioException(
                    "Registre as observações pré-operatórias antes de iniciar a cirurgia.");
        }
        this.dataHoraInicioEfetivo = LocalDateTime.now();
        this.status = StatusProcedimento.EM_ANDAMENTO;
    }

    public void concluir(String observacoesPos) {
        if (status != StatusProcedimento.EM_ANDAMENTO) {
            throw new RegraNegocioException(
                    "Apenas cirurgias em andamento podem ser concluídas.");
        }
        this.observacoesPosOperatorias = observacoesPos;
        this.dataHoraFimEfetivo = LocalDateTime.now();
        this.status = StatusProcedimento.CONCLUIDO;
    }

    public void cancelar() {
        if (status == StatusProcedimento.CONCLUIDO || status == StatusProcedimento.CANCELADO) {
            throw new RegraNegocioException(
                    "Cirurgias concluídas ou já canceladas não podem ser canceladas.");
        }
        this.status = StatusProcedimento.CANCELADO;
    }

    @Override
    public Paciente getPaciente() { return internacao != null ? internacao.getPaciente() : null; }
    @Override
    public Profissional getProfissional() { return cirurgiao; }
    public Internacao getInternacao() { return internacao; }
    public Cirurgiao getCirurgiao() { return cirurgiao; }
    public String getDescricao() { return descricao; }
    public TipoCirurgia getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    @Override
    public LocalDateTime getDataHoraPrevista() { return dataHora; }
    @Override
    public String getSituacao() { return status.name(); }
    public LocalDateTime getDataHoraInicioEfetivo() { return dataHoraInicioEfetivo; }
    public LocalDateTime getDataHoraFimEfetivo() { return dataHoraFimEfetivo; }
    public StatusProcedimento getStatus() { return status; }
    public String getObservacoesPreOperatorias() { return observacoesPreOperatorias; }
    public String getObservacoesPosOperatorias() { return observacoesPosOperatorias; }
}
