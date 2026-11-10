package com.example.MedTech.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.ProcedimentoResponse;
import com.example.MedTech.dto.ProfissionalDTO;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.StatusProcedimento;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ConsultaRepository;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.ProcedimentoCirurgicoRepository;
import com.example.MedTech.repository.ProfissionalRepository;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ConsultaRepository consultaRepository;
    private final InternacaoRepository internacaoRepository;
    private final ProcedimentoCirurgicoRepository procedimentoRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository,
            ConsultaRepository consultaRepository,
            InternacaoRepository internacaoRepository,
            ProcedimentoCirurgicoRepository procedimentoRepository) {
        this.profissionalRepository = profissionalRepository;
        this.consultaRepository = consultaRepository;
        this.internacaoRepository = internacaoRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<Profissional> listarTodos() {
        return profissionalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Profissional> buscarPorId(Long id) {
        return profissionalRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Profissional> buscarPorEspecialidade(Especialidade especialidade) {
        return profissionalRepository.findByEspecialidade(especialidade);
    }

    public Profissional salvar(Profissional profissional) {
        if (profissionalRepository.existsByCrm(profissional.getCrm())) {
            throw new RegraNegocioException("Já existe um profissional com este registro (CRM).");
        }
        return profissionalRepository.save(profissional);
    }

    /** Atualiza os dados comuns; a categoria do profissional não pode ser trocada. */
    public Profissional atualizar(Long id, ProfissionalDTO dto) {
        Profissional profissional = profissionalRepository.findById(id)
                .map(p -> (Profissional) Hibernate.unproxy(p))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));

        if (profissionalRepository.existsByCrmAndIdNot(dto.getCrm(), id)) {
            throw new RegraNegocioException("Já existe outro profissional com este registro (CRM).");
        }

        profissional.setNome(dto.getNome());
        profissional.setCpf(dto.getCpf());
        profissional.setEmail(dto.getEmail());
        profissional.setTelefone(dto.getTelefone());
        profissional.setCrm(dto.getCrm());

        if (profissional instanceof MedicoEspecialista especialista) {
            if (dto.getEspecialidade() == null) {
                throw new IllegalArgumentException(
                        "A especialidade é obrigatória para médicos especialistas.");
            }
            especialista.setEspecialidade(dto.getEspecialidade());
        }
        return profissional;
    }

    public void excluir(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Profissional não encontrado.");
        }
        if (consultaRepository.existsByProfissionalId(id)
                || internacaoRepository.existsByProfissionalId(id)
                || procedimentoRepository.existsByCirurgiaoId(id)) {
            throw new RegraNegocioException(
                    "O profissional possui consultas, internações ou procedimentos registrados e não pode ser excluído.");
        }
        profissionalRepository.deleteById(id);
    }

    /** Procedimentos do cirurgião (data, paciente e descrição); filtro opcional por situação. */
    @Transactional(readOnly = true)
    public List<ProcedimentoResponse> listarProcedimentosDoCirurgiao(Long id, StatusProcedimento status) {
        Object profissional = profissionalRepository.findById(id)
                .map(Hibernate::unproxy)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
        if (!(profissional instanceof Cirurgiao)) {
            throw new RegraNegocioException("Somente cirurgiões possuem procedimentos cirúrgicos.");
        }
        return procedimentoRepository.findByCirurgiaoIdOrderByDataHoraDesc(id).stream()
                .filter(p -> status == null || p.getStatus() == status)
                .map(ProcedimentoResponse::de)
                .toList();
    }
}
