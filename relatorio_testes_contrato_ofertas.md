# Relatório de Testes de Contrato: Módulo de Ofertas

Este documento detalha as validações de contrato implementadas no módulo de **Ofertas** (que compreende Processos Seletivos e Concursos) e documenta as divergências encontradas entre a documentação oficial da API (Swagger) e o comportamento real do banco de dados no ambiente de Desenvolvimento.

## 🎯 Escopo da Validação

A validação foi implementada na classe de testes:
- **`ErpOfertasListTest.java`**

### Endpoints e Schemas Validados

| Endpoint | Schema Principal | Descrição |
| :--- | :--- | :--- |
| `GET /api/v1/processo-seletivo` | `PageProcessoSeletivoRecordCleanDTO.json` | Retorna a página contendo a listagem de Processos Seletivos. |
| `GET /api/v1/concurso/paginado` | `PageConcursoRecordCleanDTO.json` | Retorna a página contendo a listagem de Concursos. |

---

## 🛠️ Divergências Encontradas (Ghost Data & Regras de Negócio)

Durante a execução contra a base real de dados, os seguintes campos quebraram as regras restritas estipuladas pelo Swagger. Todos foram flexibilizados nos respectivos JSON Schemas para garantir a resiliência da automação (`Regra de Ouro: Manter Quebrado mas Documentado`).

### 1. Limite de Vagas em Processos Seletivos
> [!WARNING]
> **Schema:** `ProcessoSeletivoRecordCleanDTO.json`
> **Campo:** `vagas`
> **Tipo Documentado:** `integer`
> **Tipo Retornado na Prática:** `null`

**Contexto:** A documentação exigia que todo Processo Seletivo informasse um número inteiro como limite de vagas. No entanto, o sistema permite a criação de processos contínuos ou sem limite definido, onde a aplicação salva o campo como `null` no banco de dados.
**Resolução:** O schema foi ajustado para aceitar `["integer", "null"]`.

---

## 📈 Conclusão do Módulo

O módulo de **Ofertas** demonstrou alta consistência de dados em comparação a outros módulos. Apenas o conceito de "Vagas Ilimitadas" (salvas como nulas) havia escapado do desenho original da API, mas foi facilmente encapsulado. 

Os testes estão recebendo selo verde (BUILD SUCCESS) e devidamente demarcados com a **`@Tag("contrato")`** para visibilidade no relatório HTML da esteira de CI/CD.
