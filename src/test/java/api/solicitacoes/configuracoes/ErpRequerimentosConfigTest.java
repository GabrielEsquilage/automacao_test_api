package api.solicitacoes.configuracoes;

import static org.hamcrest.Matchers.*;

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
@DisplayName("ERP - Testes de Configurações de Requerimentos/Solicitações")
@ExtendWith(ApiReportExtension.class)
public class ErpRequerimentosConfigTest {

    private static final String ID_INVALIDO = "999999999";

    @Test
    @DisplayName("Valida contrato de consulta de Requerimento/Configuração (/api/v1/requerimento/{id})")
    void testConsultaRequerimentoConfig() {
        ErpApiClient.request()
            .pathParam("id", 1) // Assume ID 1 exists
            .when()
                .get("/api/v1/requerimento/{id}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/RequerimentoRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Deve barrar tentativa de inativar um requerimento inexistente (Teste Negativo)")
    void testInativarRequerimentoInexistente() {
        ErpApiClient.request()
            .pathParam("id", ID_INVALIDO)
            .when()
                .patch("/api/v1/requerimento/{id}/inativar")
            .then()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de ativar um requerimento inexistente (Teste Negativo)")
    void testAtivarRequerimentoInexistente() {
        ErpApiClient.request()
            .pathParam("id", ID_INVALIDO)
            .when()
                .patch("/api/v1/requerimento/{id}/ativar")
            .then()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de excluir um requerimento inexistente (Teste Negativo)")
    void testExcluirRequerimentoInexistente() {
        ErpApiClient.request()
            .pathParam("id", ID_INVALIDO)
            .when()
                .delete("/api/v1/requerimento/{id}")
            .then()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de criação (POST) de requerimento com payload vazio (Teste Negativo)")
    void testCriacaoRequerimentoPayloadVazio() {
        ErpApiClient.request()
            .body("{}") // Payload vazio forçando erros de validação
            .when()
                .post("/api/v1/requerimento")
            .then()
                .statusCode(anyOf(is(400), is(422), is(415))); // Bad request ou Unprocessable Entity
    }
}
