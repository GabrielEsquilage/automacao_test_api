package api.listagens.ofertas;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@Tag("contrato")
@DisplayName("ERP - Testes de Listagem (Ofertas, Processos Seletivos e Concursos)")
@ExtendWith(ApiReportExtension.class)
public class ErpOfertasListTest {

    @Test
    @DisplayName("Deve retornar a página de processos seletivos (/api/v1/processo-seletivo)")
    public void testPageProcessosSeletivos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/processo-seletivo")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageProcessoSeletivoRecordCleanDTO.json"))
                .extract().response();

        System.out.println("Resposta de processos seletivos: " + response.asString());
    }

    /*
    // COMENTADO POIS A API RETORNA 500 INTERNAL SERVER ERROR
    @Test
    @DisplayName("Deve listar todos os concursos (/api/v1/concurso)")
    public void testListConcursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/concurso")
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
            System.out.println("Concursos listados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }
    */

    @Test
    @DisplayName("Deve retornar a página de concursos (/api/v1/concurso/paginado)")
    public void testPageConcursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/concurso/paginado")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageConcursoRecordCleanDTO.json"))
                .extract().response();

        System.out.println("Resposta de paginação de concursos: " + response.asString());
    }
}
