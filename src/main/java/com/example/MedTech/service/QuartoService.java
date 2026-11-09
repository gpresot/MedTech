package com.example.MedTech.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.dto.QuartoRequest;
import com.example.MedTech.dto.QuartoResponse;
import com.example.MedTech.enums.StatusQuarto;
import com.example.MedTech.enums.TipoQuarto;
import com.example.MedTech.exception.RecursoNaoEncontradoException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Quarto;
import com.example.MedTech.repository.InternacaoRepository;
import com.example.MedTech.repository.QuartoRepository;

@Service
@Transactional
public class QuartoService {

    private final QuartoRepository quartoRepository;
    private final InternacaoRepository internacaoRepository;

    public QuartoService(QuartoRepository quartoRepository,
            InternacaoRepository internacaoRepository) {
        this.quartoRepository = quartoRepository;
        this.internacaoRepository = internacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> listar() {
        return quartoRepository.findAll().stream().map(QuartoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> listarDisponiveis() {
        return quartoRepository.findDisponiveis().stream().map(QuartoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public QuartoResponse buscar(Long id) {
        return QuartoResponse.de(obter(id));
    }

    public QuartoResponse cadastrar(QuartoRequest req) {
        if (quartoRepository.existsByNumero(req.numero())) {
            throw new RegraNegocioException("Já existe um quarto com o número " + req.numero() + ".");
        }
        TipoQuarto tipo = req.tipo() != null ? req.tipo() : TipoQuarto.ENFERMARIA;
        Quarto quarto = new Quarto(req.numero(), req.andar(), tipo, req.capacidadeMaxima());
        return QuartoResponse.de(quartoRepository.save(quarto));
    }

    public QuartoResponse atualizar(Long id, QuartoRequest req) {
        Quarto quarto = obter(id);

        if (quartoRepository.existsByNumeroAndIdNot(req.numero(), id)) {
            throw new RegraNegocioException("Já existe outro quarto com o número " + req.numero() + ".");
        }

        TipoQuarto tipo = req.tipo() != null ? req.tipo() : quarto.getTipo();
        quarto.atualizarDados(req.numero(), req.andar(), tipo, req.capacidadeMaxima());
        return QuartoResponse.de(quarto);
    }

    /** Só permite alternar manutenção/disponível; OCUPADO é automático. */
    public QuartoResponse alterarStatus(Long id, StatusQuarto novoStatus) {
        Quarto quarto = obter(id);

        switch (novoStatus) {
            case MANUTENCAO -> quarto.colocarEmManutencao();
            case DISPONIVEL -> quarto.sairDeManutencao();
            case OCUPADO -> throw new RegraNegocioException(
                    "O status OCUPADO é definido automaticamente pela lotação do quarto.");
        }

        return QuartoResponse.de(quarto);
    }

    public void excluir(Long id) {
        Quarto quarto = obter(id);

        if (quarto.getOcupacaoAtual() > 0) {
            throw new RegraNegocioException("O quarto possui pacientes internados.");
        }
        if (internacaoRepository.existsByQuartoId(id)) {
            throw new RegraNegocioException(
                    "O quarto possui histórico de internações. Coloque-o em manutenção em vez de excluir.");
        }

        quartoRepository.delete(quarto);
    }

    private Quarto obter(Long id) {
        return quartoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Quarto " + id + " não encontrado."));
    }
}
