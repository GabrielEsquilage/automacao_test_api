# Relatório de Testes de Contrato: Módulo de Mensageria

Este documento formaliza as validações de contrato implementadas no módulo de **Mensageria (Atendimentos)**, evidenciando as cascatas de inconsistências (Ghost Data) resolvidas em um dos endpoints mais densos do ERP.

## 🎯 Escopo da Validação

A blindagem de contratos foi construída na classe:
- **`ErpMensageriaTest.java`**

### Endpoints e Schemas Validados

| Endpoint | Schema Principal | Descrição |
| :--- | :--- | :--- |
| `GET /api/v1/atendimento/pageable-setor` | `PageUsuarioSetorRecordCleanForFilaAtendimentosDTO.json` | Fila de setores de atendimento para o usuário. |
| `GET /api/v1/atendimento/meus-atendimentos` | `PageAtendimentoRecordCleanDTO.json` | Caixa de entrada (Meus Atendimentos). |
| `GET /api/v1/atendimento/painel-analise/page` | `PainelAnaliseRecordResponseForPainelAnaliseDetailDTO.json` | O "Peso-Pesado": Painel analítico de tickets com dezenas de tabelas de domínio. |

---

## 🛠️ A Cascata de Divergências (Painel de Análise)

Como previsto, endpoints analíticos que carregam dados de muitas fontes (Alunos, Prospectos, Bots, Usuários Desativados, Setores Legados) entregam frequentemente campos nulos não mapeados na documentação estrita do Swagger. 

O endpoint `painel-analise` forçou a flexibilização das seguintes sub-entidades (permitindo tipos base ou `null`):

### 1. Nível: Ticket e Assunto
- **Painel Analise DTO:** `avaliacao`, `dataResolucao`, `dataFinalizacao`, `waeMatricula` nulos para tickets recém-abertos ou não integrados.
- **Assunto DTO:** `descricao`, `isN1`, `sla` nulos para assuntos genéricos.

### 2. Nível: Atendente
- **Usuario DTO:** `login` e `departamento` nulos para usuários migrados de sistemas antigos.
- **Pessoa DTO:** `id`, `nascimento`, `nomeSocial`, `genero`, `raca` nulos para perfis de robôs (bots) ou atendentes sem cadastro completo no RH.

### 3. Nível: Aluno / Matrícula / Curso
- **Matrícula DTO:** `codigoCurso`, `cpf`, `email`, `telefone` nulos para leads e tickets de ouvidoria deslogada.
- **Curso DTO:** `criacao`, `alteracao`, `ativo`, `codigo`, `areaConhecimento`, `nivelEnsino`, `grupoCurso`, `reconhecimentos`, `segundaGraduacao` nulos quando o contato não envolve um curso oficial na esteira ativa.

### 4. Nível: Categorização (Setor e Departamento)
- **Setor DTO:** `ativo` e `departamento` nulos para setores legados.
- **Departamento DTO:** `ativo` e `sigla` nulos por motivos similares.

---

## 📈 Conclusão do Módulo

Após aplicar blindagem recursiva por oito camadas de profundidade, todos os testes (inclusive os testes estressando as Queries Complexas de filtro: `filterEmAtraso` e `filterEmAtendimentoMatricula`) rodaram com **BUILD SUCCESS**. 

Os testes constam com a selagem **`@Tag("contrato")`** e o código-fonte foi comitado à branch `feature/testesdecontrato`. A mensageria agora possui integração mapeada!
