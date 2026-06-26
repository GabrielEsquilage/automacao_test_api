# Relatório de Testes de Contrato: Módulo de Matrículas

Este documento formaliza as validações de contrato implementadas no módulo de **Matrículas** e detalha as pequenas inconsistências encontradas entre a tipagem estrita do Swagger e o formato real consumido pelos aplicativos no ambiente de Produção/Desenvolvimento.

## 🎯 Escopo da Validação

A blindagem de contratos foi injetada diretamente na classe:
- **`ErpMatriculasListTest.java`**

### Endpoints e Schemas Validados

| Endpoint | Schema Principal | Descrição |
| :--- | :--- | :--- |
| `GET /api/v1/matricula` | `PageMatriculaProcessoSeletivoRecordCleanDTO.json` | Lista as matrículas amarradas a Processos Seletivos. |
| `GET /api/v1/matricula/pageable-clean` | `PageMatriculaCleanProjection.json` | Lista genérica de matrículas (Visualização simplificada). |
| `GET /api/v1/matricula/pageable-clean-ra` | `PageMatriculaRaCleanProjection.json` | Lista genérica simplificada focada na busca por RA. |
| `GET /api/v1/rematricula/periodos` | `PageRematriculaPeriodoLetivoDTO.json` | Lista os períodos letivos vigentes para rematrícula. |

---

## 🛠️ Divergências Encontradas (Ghost Data)

Assim como visto no módulo de Ofertas, o fluxo de vida acadêmico permite exceções ("caminhos infelizes" ou fluxos secundários) que o Swagger desconhece, exigindo campos obrigatórios para situações que nem sempre se aplicam.

### 1. Ausência de ID do Candidato
> [!WARNING]
> **Schema:** `MatriculaProcessoSeletivoRecordCleanDTO.json`
> **Campo:** `candidatoId`
> **Tipo Documentado:** `integer`
> **Tipo Retornado na Prática:** `null`

**Contexto:** O Swagger documentava `candidatoId` como número inteiro não-nulo, pressupondo que toda matrícula obrigatoriamente nasceu de uma captação de candidato no processo seletivo do WAE/Portal. Contudo, matrículas manuais, transferências ou cargas antigas de sistema legado retornam `null` no banco de dados. 
**Resolução:** O campo foi flexibilizado no schema para `["integer", "null"]`.

### 2. Ausência de Limite de Vagas
> [!NOTE]
> **Schema:** `MatriculaProcessoSeletivoRecordCleanDTO.json`
> **Campo:** `vagas`
> **Tipo Documentado:** `integer`
> **Tipo Retornado na Prática:** `null`

**Contexto:** Semelhante ao módulo de Ofertas, o limite de vagas pode ser ilimitado, fazendo o ERP retornar `null` em vez de um número estrito.
**Resolução:** O campo foi flexibilizado no schema para `["integer", "null"]`.

---

## 📈 Conclusão do Módulo

A cobertura de testes para Matrículas encerra a frente de Testes de Listagens. Com isso, os endpoints mais trafegados do sistema não apenas verificam códigos `200 OK`, mas também atestam a assinatura arquitetural do banco contra a UI através do `JsonSchemaValidator`. 

Todos os testes de Matrículas constam como **BUILD SUCCESS** e a esteira exibirá os selos através da anotação **`@Tag("contrato")`**.
