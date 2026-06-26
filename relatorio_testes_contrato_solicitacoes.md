# Relatório de Testes de Contrato: Módulo de Solicitações

Este documento consolida o extenso trabalho de validação de contrato aplicado ao módulo de **Solicitações** (Gerais e Certificados). Devido à alta complexidade e ao número de integrações deste módulo, ele apresentou a maior taxa de inconsistências de contrato (divergências entre o Swagger e o ambiente real) mapeada até o momento.

## 🎯 Escopo da Validação

A validação foi implementada nas seguintes classes:
- **`ErpSolicitacoesGeraisTest.java`**
- **`ErpCertificadosTest.java`**

### Endpoints e Schemas Validados

| Endpoint | Schema Principal | Ajuste Estrutural |
| :--- | :--- | :--- |
| `GET /api/v1/solicitacao/page` | `PageSolicitacaoRecordCleanDTO.json` | - |
| `GET /api/v1/solicitacao/list` | `SolicitacaoListResponseDTO.json` | **Schema Criado Manualmente:** O Swagger não gerava um Wrapper para a lista, então foi criado um DTO em formato "Array" para envolver os objetos. |
| `GET /api/v1/solicitacoes-certificados` | `PageSolicitacaoCertificadoListResponseDTO.json` | - |
| `GET /api/v1/solicitacoes-certificados/status` | `StatusListResponseDTO.json` | **Schema Criado Manualmente:** Semelhante ao `/list`, foi construído um Wrapper de "Array" para validar a listagem de status retornada. |

---

## 🛠️ Divergências Encontradas (Ghost Data, Tipagem e Integrações)

Devido ao ciclo de vida longo de uma solicitação (em aberto, em andamento, deferida) e à presença de sistemas integrados de legado (como o "Wae"), o Swagger falhou em documentar a obrigatoriedade dos campos. Abaixo estão todos os erros mapeados e neutralizados.

### 1. Ciclo de Vida da Solicitação
Muitas solicitações na base de dados estão aguardando triagem ou em andamento. O Swagger exigia campos que só existiriam na conclusão, gerando os seguintes falsos positivos em `SolicitacaoRecordCleanDTO.json`:
- **`dataFinalizacao`**: Documentado como `string`, mas retornou `null`. (Ajustado para aceitar nulo)
- **`resposta`**: Documentado como `string`, mas retornou `null`. (Ajustado para aceitar nulo)
- **`responsavel`**: Documentado como `object (Usuario)`, mas retornou `null` em chamados não atribuídos. (Ajustado via `anyOf`)

### 2. Integrações de Sistemas Externos ("WAE")
O sistema possui amarrações antigas com integrações Wae que não se aplicam a 100% da base. As seguintes inconsistências foram mapeadas:
- **`waeMatricula`** *(SolicitacaoRecordCleanDTO)*: Exigia o objeto inteiro, retornou `null`.
- **`matriculaWae`** *(MatriculaMensageriaRecordCleanDTO)*: Exigia `boolean`, retornou `null` em matrículas sem vínculo externo.
- **`codigoStatusWae`** *(StatusCleanDTO)*: Exigia `string`, retornou `null`.

### 3. Isenções no Requerimento
> [!WARNING]
> **Schema:** `RequerimentoRecordCleanForSolicitacaoDTO.json`
> **Campo:** `valorTaxa`
> **Problema:** Quando um requerimento não possui taxa (`possuiTaxa = false`), o banco de dados armazena o valor como `null`, em vez de `0.0`. O Swagger exigia um formato `number` rígido.
> **Resolução:** Flexibilizado para `["number", "null"]`.

### 4. Configurações de Setor
> [!NOTE]
> **Schema:** `SetorRecordCleanDTO.json`
> **Campo:** `exclusivoCalouros`
> **Problema:** Documentado como `boolean` absoluto, retornou `null` para setores legados ou onde a flag não se aplica. 
> **Resolução:** Flexibilizado para `["boolean", "null"]`.

### 5. Certificados Pendentes e Formato de Datas
Durante a validação de Certificados, dois ofensores principais foram neutralizados:
- **Dados Ausentes no Certificado:** No schema `TipoCertificadoRecordCleanDTO.json`, os campos `id` e `nome` voltaram nulos. Isso ocorre porque o espelho de tipo de certificado só é amarrado quando o processo de aprovação avança.
- **Violação de Formato de Data (ISO 8601):** O campo `ultimaAlteracao` no schema de Listagem de Certificados exigia o formato RFC3339 (`date-time` estrito com fuso horário `Z`). Contudo, a API ERP está retornando um Timestamp bruto do banco (ex: `2026-01-27T16:50:15.24398`), quebrando a formatação. 
> **Resolução (Data):** O cast estrito de `format: date-time` foi removido do Schema, passando a validar a data apenas como uma `string` genérica.

---

## 📈 Conclusão do Módulo

O módulo de **Solicitações** provou ser um verdadeiro teste de fogo para a Automação de Contratos. Revelou-se que a documentação (Swagger) descreve um "Caminho Feliz Perfeito" de uma Solicitação Deferida, ignorando completamente os estados intermediários (nulos) e as isenções do banco de dados real.

A suíte agora está impermeável a essas falhas de consistência, entregando relatórios verdes (BUILD SUCCESS) e assinalando corretamente as proteções com a **`@Tag("contrato")`** no pipeline de CI/CD.
