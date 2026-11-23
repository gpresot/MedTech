# Sprint 3 — Profissionais, compatibilidade e indicadores

Diagrama de classes: [`Diagrama_de_Classes_Sprint_3.pdf`](Diagrama_de_Classes_Sprint_3.pdf) (imagem: `Diagrama_de_Classes_Sprint_3.png`; fonte Mermaid: [`diagrama-classes-sprint3.mmd`](diagrama-classes-sprint3.mmd), que o GitHub e o VS Code renderizam). Ele inclui todo o domínio da Sprint 2 mais as classes novas abaixo.

## 1. Entregas

| Item | Implementação |
|---|---|
| `ProfissionalIncompativelException` | `exception/ProfissionalIncompativelException` — estende `RegraNegocioException`, então continua virando HTTP `409` |
| Validação padronizada de profissionais | `validation/CompatibilidadeProfissional`: `exigirClinicoGeral`, `exigirEspecialista(profissional, especialidade)` e `exigirCirurgiao`. Usada por `ConsultaRotina`, `ConsultaEspecializada` e `ProcedimentoCirurgicoService.agendar` |
| Atendimentos por profissional | `IndicadoresAtendimento.porProfissional` · `GET /profissionais/indicadores/atendimentos-por-profissional[/{id}]` |
| Atendimentos por modalidade | `IndicadoresAtendimento.porModalidade` · `GET /profissionais/indicadores/atendimentos-por-modalidade` |
| `Function`, `Predicate`, `groupingBy` | `IndicadoresAtendimento`: `Function` `MODALIDADE` e `PROFISSIONAL` (como agrupar), `Predicate` `REALIZAVEL`, `CONCLUIDO` e `filtro(...)` (o que contar), `Collectors.groupingBy` aninhado (profissional → modalidade) |
| Endpoints REST | `IndicadoresProfissionalController` → `IndicadoresProfissionalService` |
| Front-end | `templates/indicadores.html` + `static/css/indicadores.css`, item **Indicadores** no menu dos doutores |
| Testes | `unit/CompatibilidadeProfissionalTest`, `unit/IndicadoresAtendimentoTest` (sem Spring) e `IndicadoresApiTests` (MockMvc + H2) |

## 2. Regras

- **Modalidades:** `ROTINA`, `ESPECIALIZADA`, `EMERGENCIA` (consultas, pelo `TipoConsulta`) e `CIRURGIA`. Todas aparecem sempre na resposta, com `0` quando não houve atendimento.
- **O que conta:** por padrão, todo atendimento que não foi cancelado nem faltou (`CANCELADA`, `CANCELADO`, `NAO_COMPARECEU` ficam de fora). Com `apenasConcluidos=true`, só `CONCLUIDA`/`CONCLUIDO`.
- **Período:** `de` e `ate` são inclusivos e opcionais; usam a data/hora prevista do atendimento.
- **Ordenação:** profissionais com mais atendimentos primeiro; empate por nome.
- **Compatibilidade:** consulta de rotina só com clínico geral; consulta especializada só com especialista da mesma especialidade; cirurgia só com cirurgião. A falha é sempre `ProfissionalIncompativelException`.

## 3. Exemplos

`GET /profissionais/indicadores/atendimentos-por-modalidade?de=2026-11-01&ate=2026-11-30`

```json
[
  { "modalidade": "ROTINA", "total": 12 },
  { "modalidade": "ESPECIALIZADA", "total": 7 },
  { "modalidade": "EMERGENCIA", "total": 3 },
  { "modalidade": "CIRURGIA", "total": 2 }
]
```

`GET /profissionais/indicadores/atendimentos-por-profissional`

```json
[
  {
    "profissionalId": 2, "nome": "Dra. Ana Souza", "tipo": "ESPECIALISTA", "total": 9,
    "porModalidade": { "ROTINA": 0, "ESPECIALIZADA": 7, "EMERGENCIA": 2, "CIRURGIA": 0 }
  }
]
```

## 4. Testes

```
./mvnw test            # Windows: .\mvnw.cmd test
```

- `CompatibilidadeProfissionalTest` — cada tipo de profissional contra cada tipo de atendimento, especialidade errada, profissional nulo, e as consultas usando a mesma validação.
- `IndicadoresAtendimentoTest` — contagem por modalidade e por profissional, ordenação, cancelados e faltas, só concluídos, período e caso sem atendimentos.
- `IndicadoresApiTests` — endpoints, parâmetros de período, `404`, permissão (paciente recebe `403`) e a tela.
