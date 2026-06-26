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

    private Integer getMatriculaIdValida() {
        // Tenta buscar o primeiro ID de matrícula de pós-graduação, se não achar, pega de graduação
        io.restassured.response.Response response = PortalApiClient.request()
            .get("/api-external/v1/portal/matricula/matricula-disciplina");
            
        Integer id = response.jsonPath().get("cursosPos[0].id");
        if (id == null) {
            id = response.jsonPath().get("cursosGrad[0].id");
        }
        return id;
    }

    @Test
    @DisplayName("Valida contrato do financeiro paginado do portal (/api-external/v1/portal/financeiro)")
    void testPortalFinanceiro() {
        Integer matriculaId = getMatriculaIdValida();
        
        PortalApiClient.request()
            .header("matriculaId", matriculaId)
            .when()
                .get("/api-external/v1/portal/financeiro")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageFinanceiroByMatriculaIdPortalProjectionDTO.json"));
    }

    @Test
    @DisplayName("Valida comportamento de fatura inexistente (Teste Negativo) (/api-external/v1/portal/financeiro/{etapaFinanceiroId})")
    void testPortalFinanceiroNotFound() {
        Integer matriculaId = getMatriculaIdValida();

        PortalApiClient.request()
            .header("matriculaId", matriculaId)
            .pathParam("etapaFinanceiroId", 99999999)
            .when()
                .get("/api-external/v1/portal/financeiro/{etapaFinanceiroId}")
            .then()
                .statusCode(404);
    }
    @Test
    @DisplayName("Valida contrato da listagem de solicitações do portal (/api-external/v1/portal/solicitacao)")
    void testPortalSolicitacao() {
        Integer matriculaId = getMatriculaIdValida();

        PortalApiClient.request()
            .header("matriculaId", matriculaId)
            .when()
                .get("/api-external/v1/portal/solicitacao")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/PageSolicitacaoPortalRecordCleanForMatriculaDTO.json"));
    }

    @Test
    @DisplayName("Valida comportamento de certificado inexistente (Teste Negativo) (/api-external/v1/portal/solicitacao-certificado/{id})")
    void testPortalSolicitacaoCertificadoNotFound() {
        PortalApiClient.request()
            .pathParam("id", 99999999)
            .when()
                .get("/api-external/v1/portal/solicitacao-certificado/{id}")
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Valida contrato do quadro de disciplinas do portal (/api-external/v1/portal/matricula/disciplina-list/{matriculaId})")
    void testPortalQuadroDisciplinas() {
        Integer matriculaId = getMatriculaIdValida();

        PortalApiClient.request()
            .pathParam("matriculaId", matriculaId)
            .when()
                .get("/api-external/v1/portal/matricula/disciplina-list/{matriculaId}")
            .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/DisciplinaListDTO.json"));
    }
}
