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
            .pathParam("id", 1)
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
            .pathParam("id", 1)
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
            .pathParam("id", 1)
            .when()
                .get("/api/v1/filial/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/FilialRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato de consulta de Departamento (/api/v1/departamento/{id})")
    void testConsultaDepartamento() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists
            .when()
                .get("/api/v1/departamento/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/DepartamentoRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato de consulta de Setor (/api/v1/setor/{id})")
    void testConsultaSetor() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists
            .when()
                .get("/api/v1/setor/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/SetorRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida comportamento de inativação de departamento inexistente (Teste Negativo)")
    void testDepartamentoInativacaoNotFound() {
        ErpApiClient.request()
            .pathParam("id", 99999999)
            .when()
                .patch("/api/v1/departamento/{id}/inativar")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida comportamento de ativação de departamento inexistente (Teste Negativo)")
    void testDepartamentoAtivacaoNotFound() {
        ErpApiClient.request()
            .pathParam("id", 99999999)
            .when()
                .patch("/api/v1/departamento/{id}/ativar")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida comportamento de inativação de setor inexistente (Teste Negativo)")
    void testSetorInativacaoNotFound() {
        ErpApiClient.request()
            .pathParam("id", 99999999)
            .when()
                .patch("/api/v1/setor/{id}/inativar")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida comportamento de ativação de setor inexistente (Teste Negativo)")
    void testSetorAtivacaoNotFound() {
        ErpApiClient.request()
            .pathParam("id", 99999999)
            .when()
                .patch("/api/v1/setor/{id}/ativar")
            .then()
                .statusCode(404);
    }
}
