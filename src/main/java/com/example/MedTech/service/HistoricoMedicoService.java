package com.example.MedTech.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.HistoricoMedicoResponse;
import com.example.MedTech.dto.InternacaoResponse;
import com.example.MedTech.dto.ProcedimentoResponse;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.model.HistoricoMedico;
import com.example.MedTech.repository.HistoricoMedicoRepository;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.ProcedimentoCirurgicoRepository;

/**
 * Monta o histórico médico reunindo as informações do paciente, suas
 * internações e seus procedimentos cirúrgicos.
 * (Consultas entram aqui quando a classe Consulta existir.)
 */
@Service
@Transactional(readOnly = true)
public class HistoricoMedicoService {

    private final HistoricoMedicoRepository historicoRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public HistoricoMedicoService(HistoricoMedicoRepository historicoRepository,
            InternacaoRepository internacaoRepository,
            ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.historicoRepository = historicoRepository;
        this.internacaoRepository = internacaoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    public HistoricoMedicoResponse consultar(Long pacienteId) {
        HistoricoMedico historico = historicoRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Histórico médico não encontrado para o paciente " + pacienteId + "."));

        var internacoes = internacaoRepository
                .findByPacienteIdOrderByDataEntradaDesc(pacienteId).stream()
                .map(InternacaoResponse::de).toList();

        var procedimentos = procedimentoRepository
                .findByInternacaoPacienteIdOrderByDataHoraDesc(pacienteId).stream()
                .map(ProcedimentoResponse::de).toList();

        return new HistoricoMedicoResponse(
                pacienteId,
                historico.getPaciente().getNome(),
                historico.getInformacoesRelevantes(),
                internacoes,
                procedimentos);
    }
}
