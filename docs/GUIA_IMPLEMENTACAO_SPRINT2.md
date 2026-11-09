# Guia de implementação — Sprint 2 (módulos do Guilherme)

Este documento explica, em linguagem direta, o que foi implementado ou preservado nos módulos **Internação**, **Quarto** e **Procedimento Cirúrgico** do MedTech. Ele é complementar ao README e serve como material de consulta para entender o código depois.

## 1. Escopo considerado

Foram considerados somente:

- entidade e regras de `Internacao`;
- entidade e regras de `Quarto`;
- entidade e regras de `ProcedimentoCirurgico`;
- regras de cirurgia;
- APIs `/internacoes`, `/quartos` e `/procedimentos`;
- testes relacionados a esses fluxos.

As APIs de pacientes, profissionais e consultas não foram tratadas como entregas deste escopo. Elas aparecem apenas como dependências: uma internação precisa referenciar paciente e profissional, e uma cirurgia precisa referenciar um cirurgião.

## 2. Resumo do que mudou

| Parte | Implementação |
|---|---|
| Tipos de quarto | Criado `TipoQuarto` com `ENFERMARIA`, `APARTAMENTO` e `UTI` e seus limites de leitos |
| Quarto | A entidade passou a armazenar o tipo e validar a capacidade de acordo com ele |
| Tipos de cirurgia | Criado `TipoCirurgia` com `ELETIVA`, `URGENCIA` e `EMERGENCIA` |
| Procedimento cirúrgico | Passou a herdar de `Atendimento`, armazenar o tipo e registrar início e fim efetivos |
| Atendimento | Criada uma classe abstrata para padronizar a identificação de paciente, profissional responsável e validação |
| DTOs | Requests e responses passaram a receber/exibir os novos campos |
| Serviços | Cadastro e atualização de quartos e agendamento de cirurgias passaram a aplicar os novos tipos e limites |
| Testes | Criados testes de API para quartos, internações e procedimentos cirúrgicos |
| Documentação | Diagrama de classes e README atualizados |

## 3. Tipos de quarto e capacidade

A enumeração `TipoQuarto` define o limite máximo de leitos:

| Tipo | Capacidade máxima |
|---|---:|
| `ENFERMARIA` | 6 |
| `APARTAMENTO` | 2 |
| `UTI` | 1 |

O limite é máximo, não uma obrigação de preencher todos os leitos. Por exemplo, uma enfermaria pode ser cadastrada com capacidade 4, mas não com capacidade 7.

Se o cadastro não informar `tipo`, o sistema considera `ENFERMARIA`. Em uma atualização, se o tipo não for informado, o sistema preserva o tipo atual do quarto. Isso também ajuda a manter compatibilidade com chamadas antigas da API.

A entidade `Quarto` continua controlando:

- `ocupacaoAtual`: quantos pacientes estão no quarto;
- `vagasDisponiveis()`: capacidade menos ocupação, ou zero em manutenção;
- `status`: `DISPONIVEL`, `OCUPADO` ou `MANUTENCAO`;
- entrada e saída de pacientes, sem ultrapassar a capacidade.

O status `OCUPADO` continua sendo calculado automaticamente. Não se deve enviar `OCUPADO` manualmente para alterar o status.

### Exemplo de cadastro de UTI

```http
POST /quartos
Content-Type: application/json
```

```json
{
  "numero": 301,
  "andar": 3,
  "capacidadeMaxima": 1,
  "tipo": "UTI"
}
```

Uma UTI com `capacidadeMaxima: 2` será rejeitada com erro de regra de negócio (`409 Conflict`).

## 4. Internações: entrada, permanência e alta

A estrutura de `Internacao` já existia e foi preservada. Ela relaciona:

- paciente internado;
- profissional responsável;
- quarto ocupado;
- data de entrada;
- previsão de alta;
- data efetiva de alta;
- observações.

Uma internação é considerada ativa quando `dataEfetivaAlta` ainda está vazia (`null`).

### Regras mantidas

1. Um paciente não pode ter duas internações ativas ao mesmo tempo.
2. A previsão de alta não pode ser anterior à entrada.
3. Ao registrar entrada, o sistema ocupa uma vaga do quarto.
4. Ao registrar alta, o sistema libera uma vaga.
5. Não é permitido dar alta enquanto houver cirurgia agendada ou em andamento vinculada à internação.

### Endpoints principais

| Método e rota | Função |
|---|---|
| `GET /internacoes` | Lista internações |
| `GET /internacoes/ativas` | Lista somente as ativas |
| `GET /internacoes/paciente/{pacienteId}` | Lista o histórico de internações de um paciente |
| `GET /internacoes/{id}` | Consulta uma internação |
| `POST /internacoes` | Registra a entrada e ocupa uma vaga |
| `PUT /internacoes/{id}/previsao-alta` | Altera a previsão de alta |
| `POST /internacoes/{id}/alta` | Registra a alta e libera a vaga |

### Exemplo de entrada

```json
{
  "pacienteId": 10,
  "profissionalId": 4,
  "quartoId": 3,
  "dataEntrada": "2026-11-09",
  "dataPrevistaAlta": "2026-11-15",
  "observacoes": "Acompanhamento clínico"
}
```

`dataEntrada` pode ser omitida; nesse caso, o serviço usa a data atual. `dataPrevistaAlta` é obrigatória.

## 5. Atendimento e herança

Foi criada a classe abstrata `Atendimento`, que funciona como uma base comum para as modalidades de atendimento. Tanto `Consulta` quanto `ProcedimentoCirurgico` herdam dela.

A classe define um contrato comum:

- toda modalidade deve informar seu paciente;
- toda modalidade deve informar o profissional responsável;
- toda modalidade deve implementar `validarAtendimento()`;
- a validação comum verifica se paciente e profissional responsável existem.

A classe usa `@MappedSuperclass`: ela participa da herança Java e do modelo JPA, mas não cria uma tabela própria. Isso evita introduzir uma nova tabela ou mexer na estrutura de persistência das consultas.

Cada modalidade mantém seus dados específicos. Uma consulta trabalha com data e horário separados; uma cirurgia trabalha com data e horário previstos em `LocalDateTime` e também possui uma internação associada. Por isso, a classe base padroniza o contrato e a validação comum, sem forçar formatos diferentes a virarem os mesmos campos.

## 6. Procedimentos cirúrgicos

`ProcedimentoCirurgico` agora herda de `Atendimento` e continua relacionado a uma `Internacao` e a um `Cirurgiao`.

### Tipos de cirurgia

A enumeração `TipoCirurgia` possui:

- `ELETIVA`;
- `URGENCIA`;
- `EMERGENCIA`.

Se o tipo não for enviado na requisição, o sistema usa `ELETIVA`.

### Datas previstas e efetivas

O procedimento possui:

- `dataHora`: data e hora previstas para a cirurgia;
- `dataHoraInicioEfetivo`: preenchida quando a cirurgia é iniciada;
- `dataHoraFimEfetivo`: preenchida quando a cirurgia é concluída.

As duas datas efetivas ficam vazias enquanto os eventos ainda não aconteceram. O início efetivo é registrado dentro de `iniciar()`; o fim efetivo, dentro de `concluir()`.

### Fluxo de status

```text
AGENDADO -> EM_ANDAMENTO -> CONCLUIDO
     \--------------------> CANCELADO
```

O sistema impede concluir um procedimento que ainda não foi iniciado. Também impede iniciar uma cirurgia cancelada ou concluída.

### Observações pré-operatórias

Conforme a decisão definida para este trabalho, as observações pré-operatórias são obrigatórias para **todos** os tipos de cirurgia, inclusive `EMERGENCIA`. Portanto, o tipo da cirurgia não pula essa validação.

### Regras de cirurgia mantidas

1. Somente um profissional do tipo `Cirurgiao` pode ser responsável.
2. O paciente precisa possuir internação ativa.
3. A data e hora previstas precisam estar no futuro no momento do agendamento.
4. O mesmo cirurgião e o mesmo paciente não podem ter outra cirurgia ativa na janela de conflito de duas horas já adotada pelo projeto.
5. A cirurgia só pode ser iniciada depois do registro das observações pré-operatórias.
6. A cirurgia só pode ser concluída depois de iniciada.
7. A internação não pode receber alta enquanto houver cirurgia agendada ou em andamento.

### Endpoints

| Método e rota | Função |
|---|---|
| `GET /procedimentos` | Lista procedimentos |
| `GET /procedimentos/internacao/{id}` | Lista por internação |
| `GET /procedimentos/paciente/{id}` | Lista por paciente |
| `GET /procedimentos/{id}` | Consulta um procedimento |
| `POST /procedimentos` | Agenda uma cirurgia |
| `PUT /procedimentos/{id}/pre-operatorio` | Registra observações pré-operatórias |
| `POST /procedimentos/{id}/iniciar` | Inicia a cirurgia |
| `POST /procedimentos/{id}/concluir` | Conclui a cirurgia e registra observações pós-operatórias |
| `POST /procedimentos/{id}/cancelar` | Cancela a cirurgia |

### Exemplo de agendamento

```json
{
  "internacaoId": 15,
  "cirurgiaoId": 8,
  "descricao": "Procedimento cirúrgico",
  "tipo": "ELETIVA",
  "dataHora": "2026-12-10T09:00:00",
  "observacoesPreOperatorias": "Avaliação pré-operatória registrada"
}
```

## 7. Como interpretar os DTOs

DTO significa *Data Transfer Object*. São objetos usados para receber dados da API e devolver respostas sem expor diretamente as entidades JPA.

- `QuartoRequest`: recebe número, andar, capacidade e tipo.
- `QuartoResponse`: devolve o tipo, capacidade, ocupação, vagas disponíveis e status.
- `ProcedimentoRequest`: recebe internação, cirurgião, descrição, data/hora prevista, observações pré-operatórias e tipo.
- `ProcedimentoResponse`: devolve os dados do procedimento, tipo, status, observações e horários efetivos.
- `InternacaoRequest` e `InternacaoResponse`: continuam representando entrada, previsão de alta, alta efetiva e relacionamentos.

Os campos adicionados aos responses passam a aparecer no JSON. Por exemplo, `tipo`, `dataHoraInicioEfetivo` e `dataHoraFimEfetivo`.

## 8. Persistência e banco de dados

As novas colunas obrigatórias de tipo (`quartos.tipo` e `procedimentos_cirurgicos.tipo`) foram definidas com valores padrão no mapeamento JPA:

- quarto: `ENFERMARIA`;
- cirurgia: `ELETIVA`.

Isso foi feito para reduzir o risco ao atualizar tabelas já existentes no Supabase. Os campos de horário efetivo são anuláveis, porque só recebem valor quando a cirurgia realmente começa ou termina.

Se o banco já possuir quartos antigos com capacidade maior que o limite da categoria padrão `ENFERMARIA`, esses registros devem ser conferidos e ajustados antes da validação final da entrega.

## 9. Testes adicionados

Foi criado `InternacaoQuartoCirurgiaApiTests`, com cenários para:

- consultar tipo e capacidade de quarto;
- rejeitar capacidade acima do limite da categoria;
- assumir `ENFERMARIA` quando o tipo do quarto não é informado;
- impedir nova internação quando a UTI está sem vaga;
- liberar a vaga após a alta;
- registrar o tipo de cirurgia e assumir `ELETIVA` quando omitido;
- exigir observações pré-operatórias também para cirurgia de emergência;
- impedir conclusão antes do início;
- preencher os horários efetivos ao iniciar e concluir;
- rejeitar profissional que não seja cirurgião como responsável.

**Limitação da validação nesta etapa:** os testes foram escritos, mas não puderam ser executados neste ambiente. O projeto exige JDK 25; o ambiente disponível usa JDK 21, e o Maven Wrapper não conseguiu baixar a distribuição Maven por indisponibilidade de acesso ao repositório Maven. A execução final deve ser feita em uma máquina com JDK 25 e acesso às dependências.

Com JDK 25 instalado, execute na raiz do projeto:

```bash
./mvnw test
```

No Windows, use:

```powershell
.\mvnw.cmd test
```

## 10. O que não foi alterado

- Não foram reimplementados pacientes, profissionais ou consultas.
- Não foram removidas as regras de internação e cirurgia que já existiam.
- Não foram alteradas credenciais, configurações de Supabase ou arquivos de segredo.
- Não foi criada tabela nova para `Atendimento`.
- Não foi feito commit, push ou pull request no GitHub nesta etapa.

## 11. Ordem sugerida para estudar o código

1. Leia `enums/TipoQuarto.java` e `enums/TipoCirurgia.java` para entender os valores permitidos.
2. Leia `model/Atendimento.java` para entender a herança e o contrato comum.
3. Leia `model/Quarto.java` para entender a validação da capacidade.
4. Leia `model/ProcedimentoCirurgico.java` para entender o ciclo de vida da cirurgia.
5. Leia `service/QuartoService.java` e `service/ProcedimentoCirurgicoService.java` para entender como as regras chegam à API.
6. Leia os DTOs para relacionar os atributos Java com o JSON enviado e recebido.
7. Por último, leia `InternacaoQuartoCirurgiaApiTests.java` para ver exemplos concretos dos fluxos funcionando ou sendo bloqueados.
