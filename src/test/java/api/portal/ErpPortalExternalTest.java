package api.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.PortalApiClient;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@Tag("contrato")
@DisplayName("ERP - Testes da API Externa (Portal do Aluno)")
@ExtendWith(ApiReportExtension.class)
public class ErpPortalExternalTest {

    @Test
    @DisplayName("Valida contrato de pessoa logada principal do portal (/api-external/v1/portal/pessoa/principal)")
    void testPessoaPrincipal() {
        PortalApiClient.request()
            .when()
                .get("/api-external/v1/portal/pessoa/principal")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PrincipalRecordResponseDTO.json"));
    }

    @Test
    @DisplayName("Valida contrato dos dados completos de pessoa logada do portal (/api-external/v1/portal/pessoa)")
    void testPortalPessoa() {
        PortalApiClient.request()
            .when()
                .get("/api-external/v1/portal/pessoa")
            .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Valida contrato dos agendamentos de matrícula do portal (/api-external/v1/portal/tipo-atividade/agendamento-matricula)")
    void testAgendamentoMatricula() {
        PortalApiClient.request()
            .when()
                .get("/api-external/v1/portal/tipo-atividade/agendamento-matricula")
            .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Valida contrato das disciplinas e matrículas globais do portal (/api-external/v1/portal/matricula/matricula-disciplina)")
    void testPortalMatriculaDisciplina() {
        PortalApiClient.request()
            .when()
                .get("/api-external/v1/portal/matricula/matricula-disciplina")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/MatriculasForPortalLoginDTO.json"));
    }

}
