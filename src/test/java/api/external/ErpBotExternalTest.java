package api.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.module.jsv.JsonSchemaValidator;

@Tag("api")
@Tag("erp")
@Tag("contrato")
@DisplayName("ERP - Testes da API Externa (Bot)")
@ExtendWith(ApiReportExtension.class)
public class ErpBotExternalTest {

    @Test
    @DisplayName("Valida contrato das matrículas acessíveis ao Bot")
    void testBotMatriculas() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/bot/matriculas")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayMatriculaProjectionForBotMatriculaDisciplina.json"));
    }

    @Test
    @DisplayName("Valida contrato dos cursos acessíveis ao Bot")
    void testBotCursos() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/bot/cursos")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageCursoWithWaeProjectionDTO.json"));
    }

    @Test
    @DisplayName("Valida comportamento da carteirinha do Bot sem informar RA (Teste Negativo)")
    void testBotCarteirinhaSemRa() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/bot/carteirinha")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida comportamento da carteirinha do Bot com RA inválido (Teste Negativo)")
    void testBotCarteirinhaRaInvalido() {
        ErpApiClient.request()
            .queryParam("ra", "RA_INEXISTENTE_999999")
            .when()
                .get("/api-external/v1/bot/carteirinha")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida contrato dos assuntos de mensageria do Bot")
    void testBotAssuntosMensageria() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/bot/assuntos-mensageria")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayResponseForBotDTO.json"));
    }

}
