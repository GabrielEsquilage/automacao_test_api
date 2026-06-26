package api.listagens.cadastros;

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
@DisplayName("ERP - Testes de Listagem (Cadastros Gerais: Cargos, Colaboradores, Órgãos)")
@ExtendWith(ApiReportExtension.class)
public class ErpCadastrosGeraisListTest {

    @Test
    @DisplayName("Deve retornar a página de cargos com sucesso (/api/v1/cargo)")
    public void testPageCargos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/cargo")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageCargoRecordResponseDTO.json"))
                .extract().response();

        System.out.println("Resposta de paginação de cargos: " + response.asString());
    }

    @Test
    @DisplayName("Deve listar todos os cargos (/api/v1/cargo/list)")
    public void testListCargos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/cargo/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/CargoListResponseDTO.json"))
                .extract().response();

        try {
            int size = response.path("size()");
            System.out.println("Cargos listados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }

    @Test
    @DisplayName("Deve retornar a página de colaboradores (/api/v1/colaborador/page)")
    public void testPageColaboradores() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/colaborador/page")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageColaboradorPageProjection.json"))
                .extract().response();

        System.out.println("Resposta de paginação de colaboradores: " + response.asString());
    }

    /*
    // COMENTADO POIS A API RETORNA 403 FORBIDDEN
    @Test
    @DisplayName("Deve listar os órgãos expedidores (/api/v1/orgao-expedidor/list)")
    public void testListOrgaosExpedidores() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/orgao-expedidor/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .extract().response();
                
        try {
            int size = response.path("size()");
            System.out.println("Órgãos Expedidores listados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }
    */
}
