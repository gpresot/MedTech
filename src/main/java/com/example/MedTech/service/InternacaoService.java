package com.example.MedTech.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.InternacaoRequest;
import com.example.MedTech.dto.InternacaoResponse;
import com.example.MedTech.enums.StatusAtendimento;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Internacao;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.model.Quarto;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.PacienteRepository;
import com.example.MedTech.repository.ProcedimentoCirurgicoRepository;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.repository.QuartoRepository;

@Service
@Transactional
public class InternacaoService {

    private final InternacaoRepository internacaoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final QuartoRepository quartoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public InternacaoService(InternacaoRepository internacaoRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            QuartoRepository quartoRepository,
            ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.internacaoRepository = internacaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.quartoRepository = quartoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listar() {
        return internacaoRepository.findAll().stream().map(InternacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listarAtivas() {
        return internacaoRepository.findByDataEfetivaAltaIsNullOrderByDataEntradaDesc()
                .stream().map(InternacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listarPorPaciente(Long pacienteId) {
        return internacaoRepository.findByPacienteIdOrderByDataEntradaDesc(pacienteId)
                .stream().map(InternacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InternacaoResponse buscar(Long id) {
        return InternacaoResponse.de(obter(id));
    }

    public InternacaoResponse registrarEntrada(InternacaoRequest req) {
        Paciente paciente = pacienteRepository.findById(req.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + req.pacienteId() + " não encontrado."));

        Profissional profissional = profissionalRepository.findById(req.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional " + req.profissionalId() + " não encontrado."));

        if (internacaoRepository.existsByPacienteIdAndDataEfetivaAltaIsNull(paciente.getId())) {
            throw new RegraNegocioException("O paciente já possui uma internação ativa.");
        }

        LocalDate entrada = req.dataEntrada() != null ? req.dataEntrada() : LocalDate.now();

        if (req.dataPrevistaAlta().isBefore(entrada)) {
            throw new RegraNegocioException(
                    "A previsão de alta não pode ser anterior à data de entrada.");
        }

        // Trava o quarto para que duas internações não disputem a mesma vaga.
        Quarto quarto = quartoRepository.findByIdParaAtualizar(req.quartoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Quarto " + req.quartoId() + " não encontrado."));

        quarto.adicionarPaciente();

        Internacao internacao = new Internacao(paciente, profissional, quarto,
                entrada, req.dataPrevistaAlta(), req.observacoes());

        return InternacaoResponse.de(internacaoRepository.save(internacao));
    }

    public InternacaoResponse alterarPrevisaoAlta(Long id, LocalDate novaData) {
        Internacao internacao = obter(id);
        internacao.alterarPrevisaoAlta(novaData);
        return InternacaoResponse.de(internacao);
    }

    public InternacaoResponse registrarAlta(Long id, LocalDate dataAlta) {
        Internacao internacao = obter(id);

        if (internacao.estaAtiva() && procedimentoRepository.existsByInternacaoIdAndStatusIn(
                id, List.of(StatusAtendimento.AGENDADO, StatusAtendimento.EM_ANDAMENTO))) {
            throw new RegraNegocioException(
                    "Não é possível dar alta com cirurgias agendadas ou em andamento. "
                            + "Conclua ou cancele os procedimentos antes.");
        }

        internacao.registrarAlta(dataAlta != null ? dataAlta : LocalDate.now());

        // Libera a vaga do quarto.
        Quarto quarto = quartoRepository.findByIdParaAtualizar(internacao.getQuarto().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto não encontrado."));
        quarto.removerPaciente();

        return InternacaoResponse.de(internacao);
    }

    private Internacao obter(Long id) {
        return internacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Internação " + id + " não encontrada."));
    }
}
