package api.listagens.academico;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@DisplayName("ERP - Testes de Listagem (Tipos Acadêmicos e Ementas)")
@ExtendWith(ApiReportExtension.class)
public class ErpTiposAcademicosListTest {

    @Test
    @DisplayName("Deve retornar a página de tipos de atividade (/api/v1/tipo-atividade)")
    public void testPageTipoAtividade() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/tipo-atividade")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Resposta de paginação de tipos de atividade: " + response.asString());
    }

    @Test
    @DisplayName("Deve listar todos os tipos de atividade (/api/v1/tipo-atividade/list)")
    public void testListTipoAtividade() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/tipo-atividade/list")
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
            System.out.println("Tipos de atividade listados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }

    @Test
    @DisplayName("Deve retornar a página de tipos de disciplina (/api/v1/tipo-disciplina)")
    public void testPageTipoDisciplina() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/tipo-disciplina")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Resposta de paginação de tipos de disciplina: " + response.asString());
    }

    @Test
    @DisplayName("Deve listar todos os tipos de disciplina (/api/v1/tipo-disciplina/list)")
    public void testListTipoDisciplina() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/tipo-disciplina/list")
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
            System.out.println("Tipos de disciplina listados: " + size);
        } catch (Exception e) {
            System.out.println("O retorno pode não ser um array JSON. Body: " + response.asString());
        }
    }

    @Test
    @DisplayName("Deve retornar a página do banco de ementas (/api/v1/ementa)")
    public void testPageEmentas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/ementa")
            .then()
                .log().status()
                .statusCode(200)
                // Valida a estrutura da paginação
                .body("$", hasKey("content"))
                .body("content", isA(java.util.List.class))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .extract().response();

        System.out.println("Resposta de paginação de ementas: " + response.asString());
    }
}
