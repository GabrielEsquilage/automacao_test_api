package api.solicitacoes.certificados;

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
@DisplayName("ERP - Testes de Certificados (Leitura e Ações Críticas)")
@ExtendWith(ApiReportExtension.class)
public class ErpCertificadosTest {

    private static final String ID_INVALIDO = "999999999";

    // ==========================================
    // TESTES POSITIVOS (LEITURA SEGURA)
    // ==========================================

    @Test
    @DisplayName("Deve retornar a página de solicitações de certificados com sucesso")
    public void testPageSolicitacoesCertificados() {
        ErpApiClient.request()
            .when()
                .get("/api/v1/solicitacoes-certificados")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageSolicitacaoCertificadoListResponseDTO.json"));
    }

    @Test
    @DisplayName("Deve retornar os status disponíveis de certificados com sucesso")
    public void testStatusSolicitacoesCertificados() {
        ErpApiClient.request()
            .when()
                .get("/api/v1/solicitacoes-certificados/status")
            .then()
                .log().status()
                .statusCode(200)
                // Geralmente retorna um Array ou objeto com os status
                .body("$", notNullValue())
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/StatusListResponseDTO.json"));
    }

    // ==========================================
    // TESTES NEGATIVOS (AÇÕES CRÍTICAS)
    // ==========================================

    @Test
    @DisplayName("Deve barrar tentativa de aprovar certificado inexistente")
    public void testAprovarCertificadoInvalido() {
        ErpApiClient.request()
            .when()
                .post("/api/v1/solicitacoes-certificados/" + ID_INVALIDO + "/aprovar")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de reprovar certificado inexistente")
    public void testReprovarCertificadoInvalido() {
        ErpApiClient.request()
            .body("{ \"motivo\": \"Teste automatizado de reprovação\" }") // Mocking um body
            .when()
                .post("/api/v1/solicitacoes-certificados/" + ID_INVALIDO + "/reprovar")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de revogar certificado inexistente")
    public void testRevogarCertificadoInvalido() {
        ErpApiClient.request()
            .body("{ \"motivo\": \"Teste automatizado de revogação\" }")
            .when()
                .post("/api/v1/solicitacoes-certificados/" + ID_INVALIDO + "/revogar")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de reemitir certificado inexistente")
    public void testReemitirCertificadoInvalido() {
        ErpApiClient.request()
            .when()
                .post("/api/v1/solicitacoes-certificados/" + ID_INVALIDO + "/reemitir")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }
}
