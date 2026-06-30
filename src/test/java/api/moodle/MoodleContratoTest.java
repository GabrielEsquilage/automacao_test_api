package api.moodle;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.MoodleApiClient;

@Tag("api")
@Tag("moodle")
@DisplayName("Moodle - Testes de Integração e Contrato (GET e Erros)")
@ExtendWith(ApiReportExtension.class)
public class MoodleContratoTest {

    @Test
    @DisplayName("Deve retornar as informações do site e usuário (core_webservice_get_site_info)")
    void testGetSiteInfo() {
        MoodleApiClient.request()
            .queryParam("wsfunction", "core_webservice_get_site_info")
            .when()
                .post("/webservice/rest/server.php")
            .then()
                .statusCode(200)
                .body("$", not(hasKey("exception")))
                .body("$", hasKey("sitename"))
                .body("$", hasKey("username"))
                .body("$", hasKey("functions"));
    }

    @Test
    @DisplayName("Teste Negativo: Buscar usuário inexistente deve retornar array vazio em vez de erro (core_user_get_users_by_field)")
    void testGetUsersByFieldNotFound() {
        MoodleApiClient.request()
            .queryParam("wsfunction", "core_user_get_users_by_field")
            .queryParam("field", "username")
            .queryParam("values[0]", "usuario_invalido_teste_qa")
            .when()
                .post("/webservice/rest/server.php")
            .then()
                .statusCode(200)
                .body("$", not(hasKey("exception")))
                // O retorno deve ser um array vazio []
                .body("$", empty());
    }

    @Test
    @DisplayName("Teste Negativo: Criação de usuário sem enviar array obrigatório (core_user_create_users)")
    void testCreateUserMissingParamsError() {
        MoodleApiClient.request()
            .queryParam("wsfunction", "core_user_create_users")
            .when()
                .post("/webservice/rest/server.php")
            .then()
                .statusCode(200)
                .body("exception", containsString("invalid_parameter_exception"))
                .body("errorcode", equalTo("invalidparameter"));
    }

    @Test
    @DisplayName("Teste Negativo: Duplicar curso/disciplina sem dados obrigatórios (core_course_duplicate_course)")
    void testDuplicateCourseMissingParamsError() {
        MoodleApiClient.request()
            .queryParam("wsfunction", "core_course_duplicate_course")
            .when()
                .post("/webservice/rest/server.php")
            .then()
                .statusCode(200)
                .body("exception", containsString("invalid_parameter_exception"))
                .body("errorcode", equalTo("invalidparameter"));
    }

    @Test
    @DisplayName("Teste Negativo: Consultar notas sem informar o courseid obrigatório (gradereport_user_get_grade_items)")
    void testGetGradeItemsMissingCourseIdError() {
        MoodleApiClient.request()
            .queryParam("wsfunction", "gradereport_user_get_grade_items")
            .when()
                .post("/webservice/rest/server.php")
            .then()
                .statusCode(200)
                .body("exception", containsString("invalid_parameter_exception"))
                .body("errorcode", equalTo("invalidparameter"));
    }
}
