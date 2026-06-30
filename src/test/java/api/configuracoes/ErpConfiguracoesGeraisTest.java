package api.configuracoes;

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
@DisplayName("ERP - Testes de Configurações Gerais (Mantenedora, Unidade e Filial)")
@ExtendWith(ApiReportExtension.class)
public class ErpConfiguracoesGeraisTest {

    @Test
    @DisplayName("Valida contrato de consulta de Mantenedora (/api/v1/mantenedora/{id})")
    void testConsultaMantenedora() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists as master
            .when()
                .get("/api/v1/mantenedora/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/MantenedoraRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato de consulta de Unidade de Ensino (/api/v1/unidade/{id})")
    void testConsultaUnidade() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists
            .when()
                .get("/api/v1/unidade/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/UnidadeRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato de consulta de Filial/Polo (/api/v1/filial/{id})")
    void testConsultaFilial() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists
            .when()
                .get("/api/v1/filial/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/FilialRecordResponseDTO.json"));
    }
}
