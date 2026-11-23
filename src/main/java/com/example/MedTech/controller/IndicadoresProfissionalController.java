package com.example.MedTech.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.MedTech.dto.AtendimentosPorModalidadeResponse;
import com.example.MedTech.dto.AtendimentosPorProfissionalResponse;
import com.example.MedTech.service.IndicadoresProfissionalService;

/**
 * Indicadores de atendimento. Parâmetros opcionais em todos os endpoints:
 * {@code de} e {@code ate} (AAAA-MM-DD, inclusivos) e
 * {@code apenasConcluidos} (padrão false: conta tudo que não foi cancelado
 * nem falta).
 */
@RestController
@RequestMapping("/profissionais/indicadores")
public class IndicadoresProfissionalController {

    private final IndicadoresProfissionalService indicadoresService;

    public IndicadoresProfissionalController(IndicadoresProfissionalService indicadoresService) {
        this.indicadoresService = indicadoresService;
    }

    @GetMapping("/atendimentos-por-profissional")
    public List<AtendimentosPorProfissionalResponse> porProfissional(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "false") boolean apenasConcluidos) {

        return indicadoresService.atendimentosPorProfissional(de, ate, apenasConcluidos);
    }

    @GetMapping("/atendimentos-por-profissional/{profissionalId}")
    public AtendimentosPorProfissionalResponse doProfissional(
            @PathVariable Long profissionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "false") boolean apenasConcluidos) {

        return indicadoresService.atendimentosDoProfissional(profissionalId, de, ate, apenasConcluidos);
    }

    @GetMapping("/atendimentos-por-modalidade")
    public List<AtendimentosPorModalidadeResponse> porModalidade(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "false") boolean apenasConcluidos) {

        return indicadoresService.atendimentosPorModalidade(de, ate, apenasConcluidos);
    }
}
