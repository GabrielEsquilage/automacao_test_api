package api.listagens.pessoas;

import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@Tag("contrato")
@DisplayName("ERP - Testes de Listagem (Pessoas e Usuários)")
@ExtendWith(ApiReportExtension.class)
public class ErpPessoasListTest {

    @Test
    @DisplayName("Deve retornar a página de pessoas com sucesso (/api/v1/pessoa)")
    public void testPagePessoas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/pessoa")
            .then()
                .log().status()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/PagePessoaRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a página customizada de pessoas (/api/v1/pessoa/page-custom)")
    public void testPageCustomPessoas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/pessoa/page-custom")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o Contrato exato via JSON Schema
                .body(matchesJsonSchemaInClasspath("schemas/PagePessoaRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a página de usuários com sucesso (/api/v1/usuario)")
    public void testPageUsuarios() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/usuario")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o Contrato exato via JSON Schema
                .body(matchesJsonSchemaInClasspath("schemas/PageUsuarioRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a lista limpa de usuários (/api/v1/usuario/clean)")
    public void testListUsuariosClean() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/usuario/clean")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();
                
        try {
            int size = response.path("size()");
            System.out.println("Usuários (clean) retornados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }
}
