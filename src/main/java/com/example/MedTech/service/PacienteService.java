package com.example.MedTech.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.InformacaoRequest;
import com.example.MedTech.dto.PacienteRequest;
import com.example.MedTech.dto.PacienteResponse;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.HistoricoMedico;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.repository.HistoricoMedicoRepository;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.PacienteRepository;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final HistoricoMedicoRepository historicoRepository;
    private final InternacaoRepository internacaoRepository;

    public PacienteService(PacienteRepository pacienteRepository,
            HistoricoMedicoRepository historicoRepository,
            InternacaoRepository internacaoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.historicoRepository = historicoRepository;
        this.internacaoRepository = internacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        return pacienteRepository.findAll().stream()
                .map(PacienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscar(Long id) {
        return PacienteResponse.de(obter(id));
    }

    public PacienteResponse cadastrar(PacienteRequest req) {
        String email = normalizar(req.email());

        if (pacienteRepository.existsByCpf(req.cpf())) {
            throw new RegraNegocioException("Já existe um paciente com este CPF.");
        }
        if (pacienteRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe um paciente com este e-mail.");
        }

        // O construtor já cria o HistoricoMedico (salvo em cascata).
        Paciente paciente = new Paciente(req.nome().trim(), req.cpf(),
                req.dataNascimento(), req.telefone(), req.endereco().trim(), email);

        return PacienteResponse.de(pacienteRepository.save(paciente));
    }

    public PacienteResponse atualizar(Long id, PacienteRequest req) {
        Paciente paciente = obter(id);
        String email = normalizar(req.email());

        if (pacienteRepository.existsByCpfAndIdNot(req.cpf(), id)) {
            throw new RegraNegocioException("Já existe outro paciente com este CPF.");
        }
        if (pacienteRepository.existsByEmailAndIdNot(email, id)) {
            throw new RegraNegocioException("Já existe outro paciente com este e-mail.");
        }

        paciente.setNome(req.nome().trim());
        paciente.setCpf(req.cpf());
        paciente.setDataNascimento(req.dataNascimento());
        paciente.setEndereco(req.endereco().trim());
        paciente.atualizarContato(req.telefone(), email);

        return PacienteResponse.de(paciente);
    }

    /** Usado pela página de perfil: o paciente só altera telefone e endereço. */
    public PacienteResponse atualizarContatoPorEmail(String email, String telefone, String endereco) {
        Paciente paciente = pacienteRepository.findByEmail(normalizar(email))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado para este usuário."));

        if (telefone == null || !telefone.matches("\\d{10,11}")) {
            throw new RegraNegocioException("O telefone deve conter 10 ou 11 números (com DDD).");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new RegraNegocioException("O endereço é obrigatório.");
        }

        // O e-mail é o login da conta, por isso não é alterado aqui.
        paciente.atualizarContato(telefone, paciente.getEmail());
        paciente.setEndereco(endereco.trim());

        return PacienteResponse.de(paciente);
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorEmail(String email) {
        return pacienteRepository.findByEmail(normalizar(email))
                .map(PacienteResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado para este usuário."));
    }

    /**
     * Contas de paciente criadas antes da integração do cadastro têm login mas
     * não têm registro de Paciente. Aqui o paciente completa os dados.
     */
    public PacienteResponse completarCadastro(String email, String nome, String cpf,
            LocalDate dataNascimento, String telefone, String endereco) {

        String emailNormalizado = normalizar(email);

        if (pacienteRepository.existsByEmail(emailNormalizado)) {
            throw new RegraNegocioException("Seu cadastro já está completo.");
        }
        if (nome == null || nome.isBlank() || endereco == null || endereco.isBlank()) {
            throw new RegraNegocioException("Preencha todos os campos obrigatórios.");
        }
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new RegraNegocioException("O CPF deve possuir 11 números.");
        }
        if (telefone == null || !telefone.matches("\\d{10,11}")) {
            throw new RegraNegocioException("O telefone deve conter 10 ou 11 números (com DDD).");
        }
        if (dataNascimento == null || !dataNascimento.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de nascimento deve estar no passado.");
        }
        if (pacienteRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe um paciente cadastrado com este CPF.");
        }

        Paciente paciente = new Paciente(nome.trim(), cpf, dataNascimento,
                telefone, endereco.trim(), emailNormalizado);

        return PacienteResponse.de(pacienteRepository.save(paciente));
    }

    public void excluir(Long id) {
        Paciente paciente = obter(id);

        // Prontuário com internações não pode ser apagado.
        if (internacaoRepository.existsByPacienteId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir um paciente que possui internações registradas.");
        }

        pacienteRepository.delete(paciente);
    }

    public void adicionarInformacao(Long pacienteId, InformacaoRequest req) {
        obter(pacienteId);

        HistoricoMedico historico = historicoRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Histórico médico não encontrado para o paciente " + pacienteId + "."));

        historico.adicionarInformacao(req.informacao());
    }

    Paciente obter(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente " + id + " não encontrado."));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
