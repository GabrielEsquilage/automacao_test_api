package api.listagens.academico;

import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@Tag("contrato")
@DisplayName("ERP - Testes de Listagem (Acadêmico/Oferta)")
@ExtendWith(ApiReportExtension.class)
public class ErpAcademicoListTest {

    @Test
    @DisplayName("Deve listar os cursos com sucesso (/api/v1/curso/list)")
    public void testListarCursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curso/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o contrato via JSON Schema (Array de Cursos)
                .body(matchesJsonSchemaInClasspath("schemas/ArrayCursoRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a página de cursos com sucesso (/api/v1/curso/page)")
    public void testPageCursos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curso/page")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o Contrato via JSON Schema (Paginação de Cursos)
                .body(matchesJsonSchemaInClasspath("schemas/PageCursoRecordResponseForPageDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve listar os curriculos com sucesso (/api/v1/curriculo/list)")
    public void testListarCurriculos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curriculo/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o contrato via JSON Schema (Array de Currículos)
                .body(matchesJsonSchemaInClasspath("schemas/ArrayCurriculoRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a página de currículos com sucesso (/api/v1/curriculo)")
    public void testPageCurriculos() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/curriculo")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o Contrato via JSON Schema (Paginação de Currículos)
                .body(matchesJsonSchemaInClasspath("schemas/PageCurriculoProjectionForPageDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve listar as disciplinas com sucesso (/api/v1/disciplina/list)")
    public void testListarDisciplinas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/disciplina/list")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o contrato via JSON Schema (Array de Disciplinas)
                .body(matchesJsonSchemaInClasspath("schemas/ArrayDisciplinaRecordResponseDTO.json"))
                .extract().response();
    }

    @Test
    @DisplayName("Deve retornar a página de disciplinas com sucesso (/api/v1/disciplina)")
    public void testPageDisciplinas() {
        Response response = ErpApiClient.request()
            .when()
                .get("/api/v1/disciplina")
            .then()
                .log().status()
                .statusCode(200)
                // Valida o Contrato via JSON Schema (Paginação de Disciplinas)
                .body(matchesJsonSchemaInClasspath("schemas/PageDisciplinaRecordCleanDTO.json"))
                .extract().response();
    }
}
