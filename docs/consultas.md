# Consultas — Thiago

Minha parte cuida do cadastro e do controle das consultas entre pacientes e
profissionais. Cada consulta tem data, horário, motivo, observações e status.

## Como o código está dividido

- `Consulta`: guarda os dados e controla as mudanças de status.
- `ConsultaRotina`, `ConsultaEspecializada` e `AtendimentoEmergencia`: representam os três tipos de atendimento e herdam de `Consulta`.
- `StatusConsulta`: indica se a consulta está agendada, confirmada, em atendimento, concluída, cancelada ou se o paciente não compareceu.
- `TipoConsulta`: identifica o tipo escolhido no cadastro.
- `ConsultaController`: recebe os pedidos na rota `/consultas`.
- `ConsultaService`: aplica as regras, como verificar se o horário do profissional está ocupado.
- `ConsultaRepository`: consulta e salva os dados no banco.
- `ConsultaRequest` e `ConsultaResponse`: definem os dados recebidos e enviados pela API.

O pedido passa pelo controller, pelo service e pelo repository até chegar ao banco.
Consultas canceladas continuam salvas para manter o histórico do paciente.

Cada modalidade valida suas próprias condições: rotina exige clínico geral;
especializada registra a especialidade e confere se ela corresponde à do médico;
emergência exige prioridade baixa, moderada, alta ou crítica.

## O que funciona

A API permite cadastrar, listar, buscar, atualizar, cancelar, iniciar e concluir
consultas. Também permite listar as consultas de um paciente. Não é possível
agendar duas consultas para o mesmo profissional na mesma data e horário.

O projeto compilou e os 24 testes passaram usando um banco em memória. A conexão
com o Supabase também foi conferida, com cadastro de paciente, profissional e consulta fictícios.

A tela em `/consultas/pagina` continua visual, sem integração com a API.
As classes de pacientes, profissionais, internações e cirurgias foram preservadas.

Os novos caminhos são `POST /consultas/{id}/confirmar` e
`POST /consultas/{id}/nao-compareceu`. O filtro `GET /consultas?status=AGENDADA`
permite identificar quem ainda aguarda atendimento; também aceita os outros status.

No cadastro de especializada, enviar `"especialidade": "CARDIOLOGIA"`, por exemplo.
No de emergência, enviar `"prioridade": "ALTA"`. Os demais campos seguem iguais.
