package com.example.MedTech.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.ProcedimentoRequest;
import com.example.MedTech.dto.ProcedimentoResponse;
import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.Internacao;
import com.example.MedTech.model.ProcedimentoCirurgico;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.ProcedimentoCirurgicoRepository;
import com.example.MedTech.repository.ProfissionalRepository;

/**
 * Regras de cirurgia:
 *  1. só um Cirurgião pode ser responsável pelo procedimento;
 *  2. o paciente precisa ter uma internação ativa;
 *  3. a cirurgia só pode ser agendada para o futuro;
 *  4. cirurgião e paciente não podem ter outra cirurgia ativa na mesma janela
 *     de tempo (duração padrão de 2 horas);
 *  5. fluxo de status: AGENDADO -> EM_ANDAMENTO -> CONCLUIDO (ou CANCELADO);
 *  6. observações pré-operatórias são obrigatórias para iniciar;
 *  7. a internação não recebe alta com cirurgia agendada ou em andamento.
 */
@Service
@Transactional
public class ProcedimentoCirurgicoService {

    static final Duration DURACAO_PADRAO = Duration.ofHours(2);
    private static final List<StatusProcedimento> ATIVOS =
            List.of(StatusProcedimento.AGENDADO, StatusProcedimento.EM_ANDAMENTO);

    private final ProcedimentoCirurgicoRepository procedimentoRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProfissionalRepository profissionalRepository;

    public ProcedimentoCirurgicoService(ProcedimentoCirurgicoRepository procedimentoRepository,
            InternacaoRepository internacaoRepository,
            ProfissionalRepository profissionalRepository) {
        this.procedimentoRepository = procedimentoRepository;
        this.internacaoRepository = internacaoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listar() {
        return procedimentoRepository.findAll().stream().map(ProcedimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listarPorInternacao(Long internacaoId) {
        return procedimentoRepository.findByInternacaoIdOrderByDataHoraDesc(internacaoId)
                .stream().map(ProcedimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listarPorPaciente(Long pacienteId) {
        return procedimentoRepository.findByInternacaoPacienteIdOrderByDataHoraDesc(pacienteId)
                .stream().map(ProcedimentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscar(Long id) {
        return ProcedimentoResponse.de(obter(id));
    }

    public ProcedimentoResponse agendar(ProcedimentoRequest req) {
        Internacao internacao = internacaoRepository.findById(req.internacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Internação " + req.internacaoId() + " não encontrada."));

        // Regra 2
        if (!internacao.estaAtiva()) {
            throw new RegraNegocioException(
                    "A cirurgia exige que o paciente tenha uma internação ativa.");
        }

        // Regra 1 (unproxy: o Hibernate pode devolver um proxy da classe base)
        Object profissional = profissionalRepository.findById(req.cirurgiaoId())
                .map(Hibernate::unproxy)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional " + req.cirurgiaoId() + " não encontrado."));

        if (!(profissional instanceof Cirurgiao cirurgiao)) {
            throw new RegraNegocioException(
                    "Apenas profissionais do tipo Cirurgião podem ser responsáveis por cirurgias.");
        }

        // Regra 3
        if (!req.dataHora().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("A cirurgia deve ser agendada para uma data futura.");
        }

        // Regra 4
        LocalDateTime inicio = req.dataHora().minus(DURACAO_PADRAO);
        LocalDateTime fim = req.dataHora().plus(DURACAO_PADRAO);

        if (procedimentoRepository.contarConflitosDoCirurgiao(
                cirurgiao.getId(), ATIVOS, inicio, fim) > 0) {
            throw new RegraNegocioException(
                    "O cirurgião já possui outra cirurgia nesse horário.");
        }
        if (procedimentoRepository.contarConflitosDoPaciente(
                internacao.getPaciente().getId(), ATIVOS, inicio, fim) > 0) {
            throw new RegraNegocioException(
                    "O paciente já possui outra cirurgia nesse horário.");
        }

        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(internacao, cirurgiao,
                req.descricao().trim(), req.dataHora(), req.observacoesPreOperatorias());

        return ProcedimentoResponse.de(procedimentoRepository.save(procedimento));
    }

    public ProcedimentoResponse registrarObservacoesPreOperatorias(Long id, String observacoes) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.registrarObservacoesPreOperatorias(observacoes);
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse iniciar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.iniciar();
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse concluir(Long id, String observacoesPos) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.concluir(observacoesPos);
        return ProcedimentoResponse.de(procedimento);
    }

    public ProcedimentoResponse cancelar(Long id) {
        ProcedimentoCirurgico procedimento = obter(id);
        procedimento.cancelar();
        return ProcedimentoResponse.de(procedimento);
    }

    private ProcedimentoCirurgico obter(Long id) {
        return procedimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Procedimento " + id + " não encontrado."));
    }
}
