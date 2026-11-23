package com.example.MedTech.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Hibernate;

import com.example.MedTech.dto.AtendimentosPorModalidadeResponse;
import com.example.MedTech.dto.AtendimentosPorProfissionalResponse;
import com.example.MedTech.dto.ProfissionalResponse;
import com.example.MedTech.enums.TipoConsulta;
import com.example.MedTech.model.Atendimento;
import com.example.MedTech.model.Consulta;
import com.example.MedTech.model.Profissional;

/**
 * Cálculo dos indicadores de atendimento. Não acessa banco: recebe a lista de
 * atendimentos (consultas e cirurgias juntas, via {@link Atendimento}) e
 * processa com {@link Function}, {@link Predicate} e
 * {@link Collectors#groupingBy}. Por isso é testável sem Spring.
 */
public final class IndicadoresAtendimento {

        public static final String CIRURGIA = "CIRURGIA";

        /** Todas as modalidades, na ordem em que aparecem nas respostas. */
        public static final List<String> MODALIDADES = Stream.concat(
                        Arrays.stream(TipoConsulta.values()).map(Enum::name),
                        Stream.of(CIRURGIA)).toList();

        private static final Set<String> NAO_REALIZADOS = Set.of("CANCELADA", "CANCELADO", "NAO_COMPARECEU");

        private static final Set<String> CONCLUIDOS = Set.of("CONCLUIDA", "CONCLUIDO");

        // ---------- Function: como agrupar ----------

        /** Modalidade do atendimento: tipo da consulta ou CIRURGIA. */
        public static final Function<Atendimento, String> MODALIDADE = atendimento -> Hibernate
                        .unproxy(atendimento) instanceof Consulta consulta
                                        ? consulta.getTipo().name()
                                        : CIRURGIA;

        /** Quem responde pelo atendimento (chave do agrupamento por profissional). */
        public static final Function<Atendimento, ChaveProfissional> PROFISSIONAL = atendimento -> {
                Profissional profissional = (Profissional) Hibernate.unproxy(atendimento.getProfissional());
                return new ChaveProfissional(
                                profissional.getId(),
                                profissional.getNome(),
                                ProfissionalResponse.de(profissional).tipo());
        };

        public record ChaveProfissional(Long id, String nome, String tipo) {
        }

        // ---------- Predicate: o que entra na contagem ----------

        /**
         * Atendimento que de fato aconteceu ou ainda vai acontecer (não cancelado, nem
         * falta).
         */
        public static final Predicate<Atendimento> REALIZAVEL = atendimento -> !NAO_REALIZADOS
                        .contains(atendimento.getSituacao());

        public static final Predicate<Atendimento> CONCLUIDO = atendimento -> CONCLUIDOS
                        .contains(atendimento.getSituacao());

        /**
         * Monta o filtro: por padrão ignora cancelados e faltas; com
         * {@code apenasConcluidos} conta só os concluídos; {@code de}/{@code ate}
         * (inclusivos, podem ser nulos) limitam o período.
         */
        public static Predicate<Atendimento> filtro(
                        LocalDate de,
                        LocalDate ate,
                        boolean apenasConcluidos) {

                if (de != null && ate != null && de.isAfter(ate)) {
                        throw new IllegalArgumentException(
                                        "A data inicial não pode ser posterior à data final.");
                }

                Predicate<Atendimento> filtro = apenasConcluidos ? CONCLUIDO : REALIZAVEL;

                if (de != null) {
                        filtro = filtro.and(a -> {
                                LocalDateTime data = a.getDataHoraPrevista();

                                return data != null
                                                && !data.toLocalDate().isBefore(de);
                        });
                }

                if (ate != null) {
                        filtro = filtro.and(a -> {
                                LocalDateTime data = a.getDataHoraPrevista();

                                return data != null
                                                && !data.toLocalDate().isAfter(ate);
                        });
                }

                return filtro;
        }

        // ---------- Indicadores ----------

        /**
         * Atendimentos por profissional (mais atendimentos primeiro), com o detalhe por
         * modalidade.
         */
        public static List<AtendimentosPorProfissionalResponse> porProfissional(
                        Collection<? extends Atendimento> atendimentos, Predicate<Atendimento> filtro) {

                Map<ChaveProfissional, Map<String, Long>> agrupado = atendimentos.stream()
                                .map(a -> (Atendimento) a)
                                .filter(filtro)
                                .collect(Collectors.groupingBy(PROFISSIONAL,
                                                Collectors.groupingBy(MODALIDADE, Collectors.counting())));

                return agrupado.entrySet().stream()
                                .map(entrada -> {
                                        Map<String, Long> porModalidade = completarModalidades(entrada.getValue());
                                        long total = porModalidade.values().stream().mapToLong(Long::longValue).sum();
                                        ChaveProfissional chave = entrada.getKey();
                                        return new AtendimentosPorProfissionalResponse(
                                                        chave.id(), chave.nome(), chave.tipo(), total, porModalidade);
                                })
                                .sorted(Comparator.comparingLong(AtendimentosPorProfissionalResponse::total).reversed()
                                                .thenComparing(AtendimentosPorProfissionalResponse::nome,
                                                                Comparator.nullsLast(String::compareToIgnoreCase)))
                                .toList();
        }

        /** Atendimentos por modalidade; todas as modalidades aparecem, mesmo com 0. */
        public static List<AtendimentosPorModalidadeResponse> porModalidade(
                        Collection<? extends Atendimento> atendimentos, Predicate<Atendimento> filtro) {

                Map<String, Long> contagem = atendimentos.stream()
                                .map(a -> (Atendimento) a)
                                .filter(filtro)
                                .collect(Collectors.groupingBy(MODALIDADE, Collectors.counting()));

                return completarModalidades(contagem).entrySet().stream()
                                .map(e -> new AtendimentosPorModalidadeResponse(e.getKey(), e.getValue()))
                                .toList();
        }

        /**
         * Resposta para um profissional que ainda não tem nenhum atendimento no
         * período.
         */
        public static AtendimentosPorProfissionalResponse semAtendimentos(Profissional profissional) {
                Profissional real = (Profissional) Hibernate.unproxy(profissional);
                return new AtendimentosPorProfissionalResponse(
                                real.getId(), real.getNome(), ProfissionalResponse.de(real).tipo(),
                                0L, completarModalidades(Map.of()));
        }

        /**
         * Devolve as quatro modalidades, na ordem fixa, com 0 onde não houve
         * atendimento.
         */
        private static Map<String, Long> completarModalidades(Map<String, Long> contagem) {
                return MODALIDADES.stream().collect(Collectors.toMap(
                                modalidade -> modalidade,
                                modalidade -> contagem.getOrDefault(modalidade, 0L),
                                (a, b) -> a,
                                LinkedHashMap::new));
        }

        private IndicadoresAtendimento() {
        }
}
