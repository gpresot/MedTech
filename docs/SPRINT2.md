# Sprint 2 — Resumo da implementação

Endpoints, tecnologias e regras de negócio estão no [README](../README.md). Este arquivo mostra **onde cada item do enunciado foi implementado**, traz exemplos de requisição e notas de persistência e testes.

## 1. Itens do enunciado

| Enunciado | Implementação |
|---|---|
| Categorias de profissionais | `Profissional` (abstrata) → `MedicoClinicoGeral`, `MedicoEspecialista`, `Cirurgiao`; criação em `ProfissionalFactory` |
| Clínico geral encaminha para especialista | `ConsultaRotina.encaminhar(...)` · `POST /consultas/{id}/encaminhar` |
| Consultar especialistas por área | `GET /profissionais/especialidade/{especialidade}` |
| Cirurgião: procedimentos com data, paciente e descrição | `GET /profissionais/{id}/procedimentos[?status=]` |
| Modalidades de atendimento sem duplicação | `Atendimento` (abstrata, `@MappedSuperclass`) → `Consulta` (abstrata) e `ProcedimentoCirurgico`; cada modalidade implementa `validarAtendimento()` |
| Emergência com prioridade e fila | `PrioridadeAtendimento`; `GET /consultas?tipo=EMERGENCIA&status=AGENDADA` (ordenada por prioridade) |
| Situação da consulta sem texto livre | enum `StatusConsulta`, com transições validadas em `Consulta` |
| Procedimento: previsto/efetivo, status, não concluir sem iniciar | `ProcedimentoCirurgico` + enum `StatusProcedimento` |
| Tipos de quarto | enum `TipoQuarto` (limite de leitos por tipo) |
| Endpoints `/pacientes`, `/profissionais`, `/consultas`, `/internacoes`, `/quartos` | pacote `controller` (mais `/procedimentos`) |
| Diagrama de classes atualizado | `Diagrama_de_Classes_Sprint_2.pdf` |

## 2. Exemplos de requisição

Quarto (UTI com mais de 1 leito é rejeitada com `409`):

```json
{ "numero": 301, "andar": 3, "capacidadeMaxima": 1, "tipo": "UTI" }
```

Internação (`dataEntrada` é opcional; `dataPrevistaAlta` é obrigatória):

```json
{ "pacienteId": 10, "profissionalId": 4, "quartoId": 3,
  "dataPrevistaAlta": "2026-11-15", "observacoes": "Acompanhamento clínico" }
```

Cirurgia (`tipo` padrão: `ELETIVA`):

```json
{ "internacaoId": 15, "cirurgiaoId": 8, "descricao": "Apendicectomia", "tipo": "ELETIVA",
  "dataHora": "2026-12-10T09:00:00", "observacoesPreOperatorias": "Avaliação registrada" }
```

Consulta (acrescente `"especialidade"` na especializada ou `"prioridade"` na emergência):

```json
{ "pacienteId": 1, "profissionalId": 2, "data": "2026-12-01", "horario": "09:00",
  "motivo": "Check-up", "tipo": "ROTINA" }
```

Encaminhamento: `POST /consultas/{id}/encaminhar` com `{ "especialidade": "CARDIOLOGIA" }`.

## 3. Persistência

- `ddl-auto=update` cria as colunas novas. `quartos.tipo` assume `ENFERMARIA` e `procedimentos_cirurgicos.tipo` assume `ELETIVA` por padrão.
- A coluna `especialidade_encaminhada` (consultas) e os horários efetivos da cirurgia são opcionais.
- Em bancos já existentes, os valores antigos de status de consulta (`AGENDADO`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`, `CONFIRMADO`) precisam ser convertidos para `AGENDADA`, `EM_ATENDIMENTO`, `CONCLUIDA`, `CANCELADA`, `CONFIRMADA`. Quartos antigos com capacidade acima do limite do tipo também devem ser ajustados.

## 4. Testes

`ConsultaApiTests` cobre consultas, status, encaminhamento, fila de emergência, categoria dos profissionais e exclusão bloqueada. `InternacaoQuartoCirurgiaApiTests` cobre quartos, internações, cirurgias e a consulta de procedimentos do cirurgião. `MedTechApplicationTests` sobe o contexto com H2 em memória.

Com JDK 25: `./mvnw test` (Windows: `.\mvnw.cmd test`).
