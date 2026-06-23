package api.listagens.academico;

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
@DisplayName("ERP - Testes de Listagem (Acadêmico/Oferta)")
@ExtendWith(ApiReportExtension.class)
public class ErpAcademicoListTest {

    @Test
    @DisplayName("Deve listar os cursos com sucesso (/api/v1/curso/list)")
    public void testListarCursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curso/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura: deve ser um array não vazio com id
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .extract().response();

        System.out.println("Cursos retornados: " + response.path("size()"));
    }

    @Test
    @DisplayName("Deve retornar a página de cursos com sucesso (/api/v1/curso/page)")
    public void testPageCursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curso/page")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação (Page do Spring Data)
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("pageable"))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Cursos na primeira página: " + response.path("content.size()"));
    }

    @Test
    @DisplayName("Deve listar os curriculos com sucesso (/api/v1/curriculo/list)")
    public void testListarCurriculos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curriculo/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura: deve ser um array não vazio com id
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .extract().response();

        System.out.println("Curriculos retornados: " + response.path("size()"));
    }

    @Test
    @DisplayName("Deve retornar a página de currículos com sucesso (/api/v1/curriculo)")
    public void testPageCurriculos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curriculo")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Currículos na primeira página: " + response.path("content.size()"));
    }

    @Test
    @DisplayName("Deve listar as disciplinas com sucesso (/api/v1/disciplina/list)")
    public void testListarDisciplinas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/disciplina/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .extract().response();

        System.out.println("Disciplinas retornadas: " + response.path("size()"));
    }

    @Test
    @DisplayName("Deve retornar a página de disciplinas com sucesso (/api/v1/disciplina)")
    public void testPageDisciplinas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/disciplina")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Disciplinas na primeira página: " + response.path("content.size()"));
    }
}
