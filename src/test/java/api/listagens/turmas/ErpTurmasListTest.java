package api.listagens.turmas;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@DisplayName("ERP - Testes de Listagem (Turmas)")
@ExtendWith(ApiReportExtension.class)
public class ErpTurmasListTest {

    @Test
    @DisplayName("Deve retornar a página de turmas com sucesso (/api/v1/turma)")
    public void testPageTurmas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/turma")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Resposta de paginação de turmas: " + response.asString());
    }

    @Test
    @DisplayName("Deve retornar a página de turmas gerais com sucesso (/api/v1/turma/geral)")
    public void testPageTurmaGeral() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/turma/geral")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Resposta de paginação de turma geral: " + response.asString());
    }

    @Test
    @DisplayName("Deve retornar a lista limpa de turmas com sucesso (/api/v1/turma/clean)")
    public void testListTurmaClean() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/turma/clean")
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
            System.out.println("Turmas (clean) retornadas: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }
}
