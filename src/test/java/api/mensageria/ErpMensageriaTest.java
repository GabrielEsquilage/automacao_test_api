package api.mensageria;

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
@DisplayName("ERP - Testes de Mensageria (Atendimentos)")
@ExtendWith(ApiReportExtension.class)
public class ErpMensageriaTest {

    @Test
    @DisplayName("Deve retornar a fila de setores para atendimento com sucesso (/api/v1/atendimento/pageable-setor)")
    public void testFilaAtendimentoSetor() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/atendimento/pageable-setor")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageUsuarioSetorRecordCleanForFilaAtendimentosDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar meus atendimentos com sucesso (/api/v1/atendimento/meus-atendimentos)")
    public void testMeusAtendimentos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/atendimento/meus-atendimentos")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageAtendimentoRecordCleanDTO.json"))
                .extract().response();
    }
}
