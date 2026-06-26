package api.listagens.matriculas;

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
@DisplayName("ERP - Testes de Listagem (Matrículas)")
@ExtendWith(ApiReportExtension.class)
public class ErpMatriculasListTest {

    @Test
    @DisplayName("Deve retornar a página de matrículas com sucesso (/api/v1/matricula)")
    public void testPageMatriculas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/matricula")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageMatriculaProcessoSeletivoRecordCleanDTO.json"))
                .extract().response();

        System.out.println("Resposta de paginação de matrículas: " + response.asString());
    }

    @Test
    @DisplayName("Deve retornar a página clean de matrículas (/api/v1/matricula/pageable-clean)")
    public void testPageCleanMatriculas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/matricula/pageable-clean")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageMatriculaCleanProjection.json"))
                .extract().response();

        System.out.println("Resposta de paginação clean de matrículas: " + response.asString());
    }

    @Test
    @DisplayName("Deve retornar a página clean de matrículas com RA (/api/v1/matricula/pageable-clean-ra)")
    public void testPageCleanRaMatriculas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/matricula/pageable-clean-ra")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageMatriculaRaCleanProjection.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar os períodos letivos para rematrícula (/api/v1/rematricula/periodos)")
    public void testListPeriodosRematricula() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/rematricula/periodos")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageRematriculaPeriodoLetivoDTO.json"))
                .extract().response();
                
        try {
            int size = response.path("size()");
            System.out.println("Períodos de rematrícula retornados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }
}
