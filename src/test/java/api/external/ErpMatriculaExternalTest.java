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
@DisplayName("ERP - Testes da API Externa (Matricula)")
@ExtendWith(ApiReportExtension.class)
public class ErpMatriculaExternalTest {

    @Test
    @DisplayName("Valida contrato da lista de gêneros na API Externa")
    void testGendersList() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/matricula/genders")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayGeneroRecordCleanDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato da lista de raças na API Externa")
    void testRacesList() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/matricula/races")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayRacaRecordCleanDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato da lista de origens na API Externa")
    void testOriginList() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/matricula/origin")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayTipoOrigemMatriculaDisciplinaRecordCleanDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato da lista de tipos de pagamento na API Externa")
    void testPaymentTypeList() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/matricula/payment-type")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayFormaPagamentoRecordCleanDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato da lista de estados na API Externa")
    void testStatesList() {
        ErpApiClient.request()
            .when()
                .get("/api-external/v1/matricula/states")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/ArrayUfRecordCleanDTO.json"));
    }

}
