
# Automacao de API com JUnit

Este projeto usa Maven, JUnit 5 e RestAssured para executar testes de API e gerar um relatorio HTML local com visao geral e detalhes por teste.

## Pre-requisitos

- Java 17
- Maven 3.9 ou superior
- Credenciais do portal no arquivo `.env` local

## Configuracao local

O arquivo `src/test/resources/environments/local.properties` guarda configuracoes nao sensiveis do ambiente:

```properties
api.baseUri=url_base_api (dev ou prod)
api.report.maxBodyChars=6000
```

As credenciais ficam no arquivo `.env` da raiz do projeto. Esse arquivo e local e esta no `.gitignore`:

```bash
export PORTAL_LOGIN="seu-login"
export PORTAL_SENHA="sua-senha"
```

Tambem e possivel usar o formato sem `export`:

```bash
PORTAL_LOGIN="seu-login"
PORTAL_SENHA="sua-senha"
```

Se preferir nao usar `.env`, defina as variaveis no shell antes de executar:

```bash
export PORTAL_LOGIN="seu-login"
export PORTAL_SENHA="sua-senha"
```

Tambem e possivel sobrescrever a URL pelo Maven:

```bash
mvn test -Dapi.baseUri=https://sua-api.local
```

## Executar os testes

Executar toda a suite:

```bash
mvn clean test
```

Executar apenas testes com a tag `api`:

```bash
mvn test -Dgroups=api
```

Executar apenas testes com a tag `moodle`:

```bash
mvn test -Dgroups=moodle
```

## Relatorios

Depois da execucao, consulte:

- `target/api-report/index.html`: relatorio HTML local com totais, status, tags, tempo, chamadas HTTP, headers e corpos redigidos.
- `target/surefire-reports/`: relatorios padrao do Maven Surefire em XML e TXT.

O relatorio HTML remove valores sensiveis comuns, como `Authorization`, `token`, `refresh`, `senha`, `password` e cookies.

## Padrao para novos testes

Use JUnit 5 com `@Tag` e a extensao `ApiReportExtension` para que o teste apareca no relatorio:

```java
@Tag("api")
@ExtendWith(ApiReportExtension.class)
class MinhaApiTest {

    @Test
    void deveValidarContrato() {
        MetodosPublicos.buscarMatriculaId("token")
                .then()
                .statusCode(200);
    }
}
```

Ao criar novos clientes HTTP, inclua o filtro `ApiReportFilter.INSTANCE` no `given()` do RestAssured para registrar as chamadas no relatorio.
