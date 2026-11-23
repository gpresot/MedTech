package com.example.MedTech.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.AtendimentosPorModalidadeResponse;
import com.example.MedTech.dto.AtendimentosPorProfissionalResponse;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.model.Atendimento;
import com.example.MedTech.repository.ConsultaRepository;
import com.example.MedTech.repository.ProcedimentoCirurgicoRepository;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.util.IndicadoresAtendimento;

/** Busca consultas e cirurgias e entrega os indicadores de atendimento. */
@Service
@Transactional(readOnly = true)
public class IndicadoresProfissionalService {

    private final ConsultaRepository consultaRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;
    private final ProfissionalRepository profissionalRepository;

    public IndicadoresProfissionalService(ConsultaRepository consultaRepository,
            ProcedimentoCirurgicoRepository procedimentoRepository,
            ProfissionalRepository profissionalRepository) {
        this.consultaRepository = consultaRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    public List<AtendimentosPorProfissionalResponse> atendimentosPorProfissional(
            LocalDate de, LocalDate ate, boolean apenasConcluidos) {

        return IndicadoresAtendimento.porProfissional(
                todosOsAtendimentos(), IndicadoresAtendimento.filtro(de, ate, apenasConcluidos));
    }

    public AtendimentosPorProfissionalResponse atendimentosDoProfissional(
            Long profissionalId, LocalDate de, LocalDate ate, boolean apenasConcluidos) {

        var profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));

        Predicate<Atendimento> filtro = IndicadoresAtendimento.filtro(de, ate, apenasConcluidos);

        // Profissional sem atendimentos não aparece no agrupamento: devolve zerado.
        return IndicadoresAtendimento.porProfissional(todosOsAtendimentos(), filtro).stream()
                .filter(r -> r.profissionalId().equals(profissionalId))
                .findFirst()
                .orElseGet(() -> IndicadoresAtendimento.semAtendimentos(profissional));
    }

    public List<AtendimentosPorModalidadeResponse> atendimentosPorModalidade(
            LocalDate de, LocalDate ate, boolean apenasConcluidos) {

        return IndicadoresAtendimento.porModalidade(
                todosOsAtendimentos(), IndicadoresAtendimento.filtro(de, ate, apenasConcluidos));
    }

    /** Consultas e cirurgias numa só lista: as duas são {@link Atendimento}. */
    private List<Atendimento> todosOsAtendimentos() {
        List<Atendimento> todos = new ArrayList<>(consultaRepository.findAllComProfissional());
        todos.addAll(procedimentoRepository.findAllComCirurgiao());
        return todos;
    }
}
