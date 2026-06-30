package api.moodle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;

@Tag("api")
@Tag("moodle")
@DisplayName("ERP -> Moodle: Testes de Integração e Orquestração (Negativos)")
@ExtendWith(ApiReportExtension.class)
public class ErpMoodleIntegrationTest {

    private static final int FAKE_ID = 9999999;

    @Test
    @DisplayName("Teste Negativo: Tentar sincronizar matrícula inexistente no Moodle deve retornar 404/400")
    void testSyncMatriculaInexistente() {
        ErpApiClient.request()
            .pathParam("id", FAKE_ID)
            .when()
                .post("/api/v1/matricula/sincronizar-moodle/{id}")
            .then()
                .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(404), org.hamcrest.Matchers.is(400)));
    }

    @Test
    @DisplayName("Teste Negativo: Tentar sincronizar currículo inexistente no Moodle retorna 200 silencioso no ERP")
    void testSyncMatriculaByCurriculoInexistente() {
        ErpApiClient.request()
            .pathParam("curriculoId", FAKE_ID)
            .when()
                .post("/api/v1/matricula/sincronizar-moodle/curriculo/{curriculoId}")
            .then()
                .statusCode(200); // Mapeado: o ERP está engolindo o erro e retornando 200
    }

    @Test
    @DisplayName("Teste Negativo: Sincronizar Moodle de Rematrícula de período inexistente retorna 200 silencioso")
    void testSyncRematriculaPeriodoInexistente() {
        ErpApiClient.request()
            .pathParam("periodoLetivoId", FAKE_ID)
            .when()
                .post("/api/v1/rematricula/{periodoLetivoId}/sincronizar-moodle")
            .then()
                .statusCode(200); // Mapeado: o ERP está engolindo o erro e retornando 200
    }

    @Test
    @DisplayName("Teste Contrato: Rota do Scheduler de Matrícula quebra com 500 ao ser invocada sem parâmetros de job")
    void testSchedulerSyncMatriculaMoodle() {
        ErpApiClient.request()
            .when()
                .post("/api/v1/scheduler/matricula-moodle/sincronizar")
            .then()
                .statusCode(500); // Bug/Comportamento atual: ERP dá Internal Server Error
    }

    @Test
    @DisplayName("Teste Negativo: Sincronizar Processo Seletivo inexistente (Imediato) retorna 412 Precondition Failed")
    void testSyncProcessoSeletivoImediatoInexistente() {
        ErpApiClient.request()
            .pathParam("id", FAKE_ID)
            .when()
                .post("/api/v1/processo-seletivo/{id}/sincronizar-moodle-imediato")
            .then()
                .statusCode(412); // Precondition Failed
    }

    @Test
    @DisplayName("Teste Negativo: Sincronizar datas de Processo Seletivo inexistente retorna 200 silencioso")
    void testSyncDatasProcessoSeletivoInexistente() {
        ErpApiClient.request()
            .pathParam("id", FAKE_ID)
            .when()
                .post("/api/v1/processo-seletivo/{id}/sincronizar-datas-moodle")
            .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Teste Negativo: Inativar curso no Moodle sem informar body (405 Method Not Allowed)")
    void testInativarCourseMissingBody() {
        ErpApiClient.request()
            .pathParam("processoSeletivoId", FAKE_ID)
            .when()
                .patch("/api/v1/disciplina-moodle/{processoSeletivoId}/inativar-course")
            .then()
                .statusCode(405);
    }
    
    @Test
    @DisplayName("Teste Negativo: Ativar curso no Moodle sem informar body (405 Method Not Allowed)")
    void testAtivarCourseMissingBody() {
        ErpApiClient.request()
            .when()
                .patch("/api/v1/disciplina-moodle/ativar-course")
            .then()
                .statusCode(405);
    }

    @Test
    @DisplayName("Teste Negativo: Atualizar nome de Disciplina/Série inexistente (405 Method Not Allowed)")
    void testUpdateNameDisciplinaInexistente() {
        ErpApiClient.request()
            .pathParam("processoSeletivoId", FAKE_ID)
            .pathParam("serieId", FAKE_ID)
            .pathParam("disciplinaId", FAKE_ID)
            .when()
                .patch("/api/v1/disciplina-moodle/{processoSeletivoId}/serie/{serieId}/disciplina/{disciplinaId}/update-name")
            .then()
                .statusCode(405);
    }
}
