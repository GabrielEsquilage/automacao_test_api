package api.external;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import api.support.ApiConfig;
import api.support.AuthManager;
import io.restassured.module.jsv.JsonSchemaValidator;

@DisplayName("Testes de Contrato - API Externa (Matricula)")
public class ErpMatriculaExternalTest extends ApiConfig {

    @Test
    @Tag("contrato")
    @DisplayName("Valida contrato da lista de gêneros na API Externa")
    void testGendersList() {
        given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
        .when()
            .get("/api-external/v1/matricula/genders")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayGeneroRecordCleanDTO.json"));
    }

    @Test
    @Tag("contrato")
    @DisplayName("Valida contrato da lista de raças na API Externa")
    void testRacesList() {
        given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
        .when()
            .get("/api-external/v1/matricula/races")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayRacaRecordCleanDTO.json"));
    }

    @Test
    @Tag("contrato")
    @DisplayName("Valida contrato da lista de origens na API Externa")
    void testOriginList() {
        given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
        .when()
            .get("/api-external/v1/matricula/origin")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayTipoOrigemMatriculaDisciplinaRecordCleanDTO.json"));
    }

    @Test
    @Tag("contrato")
    @DisplayName("Valida contrato da lista de tipos de pagamento na API Externa")
    void testPaymentTypeList() {
        given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
        .when()
            .get("/api-external/v1/matricula/payment-type")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayFormaPagamentoRecordCleanDTO.json"));
    }

    @Test
    @Tag("contrato")
    @DisplayName("Valida contrato da lista de estados na API Externa")
    void testStatesList() {
        given()
            .header("Authorization", "Bearer " + AuthManager.getToken())
        .when()
            .get("/api-external/v1/matricula/states")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayUfRecordCleanDTO.json"));
    }

}
