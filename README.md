# MedTech

Sistema hospitalar desenvolvido como trabalho prático da disciplina **Programação Modular**.

- **Professor:** Glender
- **Alunos:**
  - Giovanni Arenare Mota
  - Guilherme Gonçalves Meireles
  - Guilherme Miranda Presot
  - Thiago Guerra de Araujo

## Sobre o projeto

O MedTech gerencia o fluxo de um hospital: cadastro de pacientes e profissionais, histórico médico, controle de quartos, internações e procedimentos cirúrgicos. A modelagem (diagrama de classes e cartões CRC) está na pasta [`docs/`](docs).

## Tecnologias

- Java 25
- Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation, Thymeleaf)
- PostgreSQL (Supabase)
- Maven (wrapper incluso)

## Como executar

1. Tenha o JDK 25 instalado.
2. Defina as variáveis de ambiente:

   | Variável        | Descrição                              |
   | --------------- | -------------------------------------- |
   | `DB_PASSWORD`   | Senha do banco PostgreSQL              |
   | `MAIL_USERNAME` | Usuário do e-mail (SMTP)               |
   | `MAIL_PASSWORD` | Senha do e-mail (SMTP)                 |
   | `APP_BASE_URL`  | Opcional. Padrão: `http://localhost:8080` |

3. Rode:

   ```bash
   ./mvnw spring-boot:run
   ```

As tabelas são criadas automaticamente (`spring.jpa.hibernate.ddl-auto=update`).

## Estrutura

```
src/main/java/com/example/MedTech
├── config        # Segurança e usuários
├── controller    # APIs REST e controllers das páginas
├── dto           # Objetos de requisição e resposta
├── enums         # Especialidade, TipoQuarto, TipoCirurgia, StatusQuarto, StatusProcedimento, TipoUsuario
├── exception     # Exceções e tratamento global (404, 409, 400)
├── factory       # ProfissionalFactory
├── model         # Entidades JPA
├── repository    # Repositórios Spring Data
└── service       # Regras de negócio
```

## Modelo de domínio

`Paciente` (1) ── (1) `HistoricoMedico` · `Paciente` (1) ── (N) `Internacao` · `Quarto` (1) ── (N) `Internacao` · `Profissional` (1) ── (N) `Internacao` · `Internacao` (1) ── (N) `ProcedimentoCirurgico` · `Cirurgiao` (1) ── (N) `ProcedimentoCirurgico`

`Atendimento` é a classe abstrata comum às consultas e aos procedimentos cirúrgicos; cada modalidade implementa sua validação específica.

`Profissional` é abstrata e tem três especializações (`MedicoClinicoGeral`, `MedicoEspecialista`, `Cirurgiao`) em tabela única.

## API

Erros: `400` validação, `404` recurso não encontrado, `409` violação de regra de negócio.

### `/profissionais`
`GET /` · `GET /{id}` · `POST /` · `PUT /{id}` · `DELETE /{id}` · `GET /especialidade/{especialidade}`

### `/pacientes`
| Método | Rota | Descrição |
| ------ | ---- | --------- |
| GET | `/pacientes` | Lista pacientes |
| GET | `/pacientes/{id}` | Busca por id |
| POST | `/pacientes` | Cadastra (cria o histórico médico junto) |
| PUT | `/pacientes/{id}` | Atualiza |
| DELETE | `/pacientes/{id}` | Exclui (bloqueado se houver internações) |
| GET | `/pacientes/{id}/historico` | Histórico: informações, internações e procedimentos |
| POST | `/pacientes/{id}/historico/informacoes` | Adiciona informação relevante |

### `/quartos`
| Método | Rota | Descrição |
| ------ | ---- | --------- |
| GET | `/quartos` | Lista quartos |
| GET | `/quartos/disponiveis` | Quartos com vaga |
| GET | `/quartos/{id}` | Busca por id |
| POST | `/quartos` | Cadastra |
| PUT | `/quartos/{id}` | Atualiza |
| PUT | `/quartos/{id}/status` | Alterna `MANUTENCAO` / `DISPONIVEL` |
| DELETE | `/quartos/{id}` | Exclui (bloqueado se ocupado ou com histórico) |

### `/internacoes`
| Método | Rota | Descrição |
| ------ | ---- | --------- |
| GET | `/internacoes` | Lista todas |
| GET | `/internacoes/ativas` | Apenas ativas |
| GET | `/internacoes/paciente/{pacienteId}` | Por paciente |
| GET | `/internacoes/{id}` | Busca por id |
| POST | `/internacoes` | Registra entrada (ocupa uma vaga) |
| PUT | `/internacoes/{id}/previsao-alta` | Altera a previsão de alta |
| POST | `/internacoes/{id}/alta` | Registra alta (libera a vaga) |

A página HTML de internações fica em `/internacoes/pagina`.

### `/procedimentos`
| Método | Rota | Descrição |
| ------ | ---- | --------- |
| GET | `/procedimentos` | Lista todos |
| GET | `/procedimentos/internacao/{id}` | Por internação |
| GET | `/procedimentos/paciente/{id}` | Por paciente |
| GET | `/procedimentos/{id}` | Busca por id |
| POST | `/procedimentos` | Agenda cirurgia |
| PUT | `/procedimentos/{id}/pre-operatorio` | Registra observações pré-operatórias |
| POST | `/procedimentos/{id}/iniciar` | Inicia |
| POST | `/procedimentos/{id}/concluir` | Conclui (com observações pós-operatórias) |
| POST | `/procedimentos/{id}/cancelar` | Cancela |

## Regras de negócio

**Quartos**
- Tipos: `ENFERMARIA` (até 6 leitos), `APARTAMENTO` (até 2) e `UTI` (até 1).
- Quando o tipo não é informado, o quarto é cadastrado como `ENFERMARIA`.
- A capacidade máxima nunca pode ser ultrapassada nem exceder o limite do tipo.
- O status `OCUPADO` é calculado pela lotação; `MANUTENCAO` só é permitido com o quarto vazio.

**Internações**
- Um paciente só pode ter uma internação ativa por vez.
- A previsão de alta não pode ser anterior à entrada.
- Não é possível dar alta com cirurgia agendada ou em andamento.

**Cirurgias**
1. Apenas `Cirurgiao` pode ser responsável.
2. O paciente precisa ter internação ativa.
3. Só pode ser agendada para uma data futura.
4. Cirurgião e paciente não podem ter outra cirurgia ativa na mesma janela de 2 horas.
5. Fluxo de status: `AGENDADO` → `EM_ANDAMENTO` → `CONCLUIDO` (ou `CANCELADO`).
6. Observações pré-operatórias são obrigatórias para iniciar, independentemente do tipo de cirurgia.
7. O tipo pode ser `ELETIVA`, `URGENCIA` ou `EMERGENCIA` (padrão: `ELETIVA`).
8. O sistema registra a data/hora efetiva de início e de conclusão.

## Pendências

- Integrar o cadastro de paciente (`/register/paciente`) à entidade `Paciente`.
- Incluir `Consulta` no histórico médico.
- Restringir o acesso às APIs por autenticação e papéis (hoje estão liberadas para testes).
