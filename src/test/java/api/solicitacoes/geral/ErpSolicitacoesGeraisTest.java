package api.solicitacoes.geral;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;

@Tag("api")
@Tag("erp")
@DisplayName("ERP - Testes de Solicitações (Leitura e Ações Críticas)")
@ExtendWith(ApiReportExtension.class)
public class ErpSolicitacoesGeraisTest {

    private static final String ID_INVALIDO = "999999999";

    // ==========================================
    // TESTES POSITIVOS (LEITURA SEGURA)
    // ==========================================

    @Test
    @DisplayName("Deve retornar a página de solicitações com sucesso")
    public void testPageSolicitacoes() {
        ErpApiClient.request()
            .when()
                .get("/api/v1/solicitacao/page")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"));
    }

    @Test
    @DisplayName("Deve retornar a lista de solicitações com sucesso")
    public void testListSolicitacoes() {
        ErpApiClient.request()
            .when()
                .get("/api/v1/solicitacao/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida que retorna um Array de objetos
                .body("$", isA(java.util.List.class));
    }

    // ==========================================
    // TESTES NEGATIVOS (AÇÕES CRÍTICAS)
    // ==========================================

    @Test
    @DisplayName("Deve barrar tentativa de deferir solicitação inexistente")
    public void testDeferirSolicitacaoInvalida() {
        ErpApiClient.request()
            .when()
                .patch("/api/v1/solicitacao/" + ID_INVALIDO + "/deferir")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de indeferir solicitação inexistente")
    public void testIndeferirSolicitacaoInvalida() {
        ErpApiClient.request()
            .body("{ \"motivo\": \"Teste automatizado\" }")
            .when()
                .patch("/api/v1/solicitacao/" + ID_INVALIDO + "/indeferir")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de aplicar devolutiva em solicitação inexistente")
    public void testDevolutivaSolicitacaoInvalida() {
        ErpApiClient.request()
            .body("{ \"mensagem\": \"Teste de devolutiva\" }")
            .when()
                .patch("/api/v1/solicitacao/" + ID_INVALIDO + "/devolutiva")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de avançar etapa de solicitação inexistente")
    public void testAvancarEtapaSolicitacaoInvalida() {
        ErpApiClient.request()
            .when()
                .patch("/api/v1/solicitacao/" + ID_INVALIDO + "/next-etapa")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }

    @Test
    @DisplayName("Deve barrar tentativa de remover responsável de solicitação inexistente")
    public void testRemoverResponsavelSolicitacaoInvalida() {
        ErpApiClient.request()
            .when()
                .patch("/api/v1/solicitacao/" + ID_INVALIDO + "/unassign-responsavel")
            .then()
                .log().status()
                .statusCode(anyOf(is(404), is(400), is(422)));
    }
}
