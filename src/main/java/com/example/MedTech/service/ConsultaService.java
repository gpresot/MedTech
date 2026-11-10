package com.example.MedTech.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.ConsultaRequest;
import com.example.MedTech.dto.ConsultaResponse;
import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.AtendimentoEmergencia;
import com.example.MedTech.model.Consulta;
import com.example.MedTech.model.ConsultaEspecializada;
import com.example.MedTech.model.ConsultaRotina;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ConsultaRepository;
import com.example.MedTech.repository.PacienteRepository;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar() {
        return converter(consultaRepository.findAll());
    }

    /** Filtra por situação e/ou modalidade (ex.: emergências ainda aguardando avaliação). */
    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarFiltrado(StatusConsulta status, TipoConsulta tipo) {
        List<Consulta> consultas = status != null
                ? consultaRepository.findByStatus(status)
                : consultaRepository.findAll();
        List<Consulta> filtradas = new ArrayList<>();
        for (Consulta consulta : consultas) {
            if (tipo == null || consulta.getTipo() == tipo) {
                filtradas.add(consulta);
            }
        }
        if (tipo == TipoConsulta.EMERGENCIA) {
            // Fila de emergência: prioridade mais alta primeiro, depois a mais antiga.
            filtradas.sort(Comparator
                    .comparing((Consulta c) -> ((AtendimentoEmergencia) c).getPrioridade(),
                            Comparator.reverseOrder())
                    .thenComparing(Consulta::getData)
                    .thenComparing(Consulta::getHorario));
        }
        return converter(filtradas);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarPorPaciente(Long pacienteId) {
        obterPaciente(pacienteId);
        return converter(consultaRepository.findByPacienteIdOrderByDataDescHorarioDesc(pacienteId));
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscar(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse agendar(ConsultaRequest req) {
        Paciente paciente = obterPaciente(req.pacienteId());
        Profissional profissional = obterProfissional(req.profissionalId());
        verificarHorario(req, null);

        Consulta consulta;
        if (req.tipo() == TipoConsulta.ROTINA) {
            consulta = new ConsultaRotina();
        } else if (req.tipo() == TipoConsulta.ESPECIALIZADA) {
            consulta = new ConsultaEspecializada();
        } else if (req.tipo() == TipoConsulta.EMERGENCIA) {
            consulta = new AtendimentoEmergencia();
        } else {
            throw new IllegalArgumentException("Informe um tipo de consulta válido.");
        }

        preencher(consulta, req, paciente, profissional);
        return ConsultaResponse.de(consultaRepository.save(consulta));
    }

    public ConsultaResponse atualizar(Long id, ConsultaRequest req) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.verificarAgendada();
        if (consulta.getTipo() != req.tipo()) {
            throw new RegraNegocioException("Não é possível alterar o tipo de uma consulta.");
        }

        Paciente paciente = obterPaciente(req.pacienteId());
        Profissional profissional = obterProfissional(req.profissionalId());
        verificarHorario(req, id);
        preencher(consulta, req, paciente, profissional);
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse iniciar(Long id) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.iniciar();
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse encaminhar(Long id, Especialidade especialidade) {
        Consulta consulta = obterParaAtualizar(id);
        Object real = Hibernate.unproxy(consulta);
        if (!(real instanceof ConsultaRotina rotina)) {
            throw new RegraNegocioException("Somente consultas de rotina podem gerar encaminhamento.");
        }
        rotina.encaminhar(especialidade);
        return ConsultaResponse.de(rotina);
    }

    public ConsultaResponse confirmar(Long id) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.confirmar();
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse registrarNaoComparecimento(Long id) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.registrarNaoComparecimento();
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse concluir(Long id, String observacoes) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.concluir(observacoes);
        return ConsultaResponse.de(consulta);
    }

    public ConsultaResponse cancelar(Long id) {
        Consulta consulta = obterParaAtualizar(id);
        consulta.cancelar();
        return ConsultaResponse.de(consulta);
    }

    private Paciente obterPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private Profissional obterProfissional(Long id) {
        // A trava impede dois agendamentos simultâneos para o mesmo profissional.
        return consultaRepository.buscarProfissionalParaAgendar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }

    private Consulta obterParaAtualizar(Long id) {
        return consultaRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }

    private void verificarHorario(ConsultaRequest req, Long consultaId) {
        if (consultaRepository.contarConflitos(req.profissionalId(), req.data(), req.horario(),
                StatusConsulta.CANCELADA, consultaId) > 0) {
            throw new RegraNegocioException("O profissional já possui uma consulta nesse horário.");
        }
    }

    private void preencher(Consulta consulta, ConsultaRequest req,
            Paciente paciente, Profissional profissional) {
        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.remarcar(req.data(), req.horario());
        consulta.setMotivo(req.motivo().trim());
        consulta.setObservacoesMedicas(req.observacoesMedicas());
        if (consulta instanceof ConsultaEspecializada) {
            ((ConsultaEspecializada) consulta).setEspecialidade(req.especialidade());
        } else if (req.especialidade() != null) {
            throw new RegraNegocioException("A especialidade deve ser informada apenas na consulta especializada.");
        }
        if (consulta instanceof AtendimentoEmergencia) {
            ((AtendimentoEmergencia) consulta).setPrioridade(req.prioridade());
        } else if (req.prioridade() != null) {
            throw new RegraNegocioException("A prioridade deve ser informada apenas na emergência.");
        }
        consulta.validarAtendimento();
    }

    private List<ConsultaResponse> converter(List<Consulta> consultas) {
        List<ConsultaResponse> respostas = new ArrayList<>();
        for (Consulta consulta : consultas) {
            respostas.add(ConsultaResponse.de(consulta));
        }
        return respostas;
    }
}
