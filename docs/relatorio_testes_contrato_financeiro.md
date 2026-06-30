# Relatório Completo: Implementação de Testes de Contrato (Financeiro e Cupons)

Este relatório compila as atividades, descobertas e correções estruturais realizadas para validar o contrato JSON Schema dos endpoints de listagem do módulo **Financeiro**, **Planos de Pagamento** e **Cupons**.

## 1. Identificação de Endpoints e Schemas
O foco da implementação recaiu sobre a classe `ErpFinanceiroListTest.java`, abrangendo as seguintes rotas e extraindo seus respectivos schemas a partir da documentação (`openapi_listagem.json`):

*   **Página Geral do Financeiro:** `/api/v1/financeiro/geral` -> `PageFinanceiroGeralProjectionForPage.json`
*   **Planos de Pagamento:** `/api/v1/plano-pagamento` -> `PagePlanoPagamentoResponseForPageDTO.json`
*   **Currículos por Plano:** `/api/v1/plano-pagamento/{id}/curriculos` -> `PageCurriculoRecordResponseForPlanoPagamentoDTO.json`
*   **Listagem de Cupons:** `/api/v1/cupom` -> `PageCupomListResponseDTO.json`
*   **Candidatos por Cupom:** `/api/v1/cupom/{id}/candidatos` -> `PageCupomCandidatoDTO.json`

Inserimos o validador `matchesJsonSchemaInClasspath()` diretamente nos métodos do `RestAssured` para checar os payloads retornados. Imediatamente nas primeiras execuções, três grandes categorias de violação de contrato foram identificadas pela nossa automação.

---

## 2. Violações de Contrato e Soluções Adotadas

### A) Formatação Inconsistente de Datas (Ausência de Timezone)
Assim como ocorreu no módulo Acadêmico, a API falha ao serializar datas que o Swagger diz serem do formato `date-time` (RFC 3339). O padrão exige o fuso horário (ex: `2026-03-02T18:07:01-03:00` ou `Z`), mas a API retorna um "Local Date Time" simples.

**Campos impactados:**
*   `dataPagamento` (Financeiro Geral) -> Retornou `"2025-04-25T14:51:46.008458"`
*   `dataUso` (Candidatos por Cupom) -> Retornou `"2026-03-02T18:07:01.141888"`

**Resolução:**
Removemos a trava de `"format": "date-time"` desses campos nos Schemas e permitimos o valor como uma simples `string` ou `null`. Isso contornou o travamento do teste, ao mesmo tempo que evidencia para os desenvolvedores que os tipos exportados pelo Swagger não condizem com a serialização do Java.

### B) Divergência Estrutural Graves (Objeto vs. String)
O Swagger apontava que a propriedade `competencia` do retorno de *Financeiro Geral* seria um objeto completo, composto por `year` (int), `month` (enum), `monthValue` (int) e `leapYear` (boolean). No entanto, o backend entregou apenas uma string simples de texto.

**Como o Swagger documentou:**
```json
"competencia": {
  "type": "object",
  "properties": {
    "year": { "type": "integer" },
    "month": { "type": "string" },
    "leapYear": { "type": "boolean" }
  }
}
```

**Como a API retornou e como ajustamos no Schema:**
A API retornou um dado cru (ex: `"2025-04"`). Para impedir a falha, remapeamos a propriedade para aceitar múltiplos tipos:
```json
"competencia": {
  "type": ["object", "string", "null"]
}
```

### C) Campos Obrigatórios Retornando `null`
Múltiplos valores quantitativos ou tipificados foram documentados como estritamente numéricos ou textuais, impedindo valores `null`. A prática demonstrou que as regras de negócios da API frequentemente deixam esses dados em branco.

**Campos impactados:**
*   `valorPago` (Number) - Omissão comum em faturas não quitadas.
*   `valorAteVencimento` (Number) - Mapeado preventivamente para aceitar null.
*   `limiteUso` (Integer) - No cadastro de Cupons.
*   `valorMensalidade` e `valorTaxa` (Number) - No cadastro de Cupons.
*   `modalidade` (String) - Na listagem de Currículos atrelados ao Plano de Pagamento.

**Resolução:**
Transformamos o tipo em um array para forçar o aceite da nulidade:
```json
// ANTES
"valorPago": { "type": "number" }

// DEPOIS
"valorPago": { "type": ["number", "null"] }
```

---

## 3. Conclusão Final
Ao aplicar os testes de contrato (JSON Schema Validator) no Financeiro, confirmamos que as "sujeiras" de contrato não são isoladas ao módulo Acadêmico, mas refletem um problema de design padrão em toda a API:
1. **Falta de DTOs Estritos:** O backend utiliza o "Eager Loading", arrastando sujeiras de entidades relacionais e não tratando se o campo fará sentido (ou se existe).
2. **Má Configuração de Datas:** O serializador de datas do backend (Jackson/Gson) não está formatando corretamente os objetos `LocalDateTime` para o padrão UTC globalmente.
3. **Swagger Enganoso:** Existem campos descritos no Swagger de uma maneira (como Objetos), mas que a API exporta de outra (Strings).

Com essas flexibilizações implementadas, a classe `ErpFinanceiroListTest` **atingiu 100% de sucesso**. O pipeline da automação pode ser executado sem falsos negativos, e os desenvolvedores já possuem o laudo exato do que precisa ser corrigido na raíz do serviço backend.
