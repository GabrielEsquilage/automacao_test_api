package api;

import api.support.ApiConfig;
import api.support.ApiReportFilter;
import io.restassured.response.Response;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class MetodosPublicos {
    private static final String LOGIN_PATH = "/api-external/v1/portal/auth/login";
    private static final String MATRICULA_PATH = "/api-external/v1/portal/matricula/matricula-disciplina";
    private static final String DISCIPLINAS_ATUAIS_PATH = "/api-external/v1/portal/moodle/disciplinas-current";
    private static final String DETALHES_MOODLE_PATH = "/api-external/v1/portal/moodle/disciplina-link/";

    public static Response realizarLoginPortalProd(String login, String senha) {
        return request()
            .body(loginPayload(login, senha))
        .when()
            .post(LOGIN_PATH)
            .then()
            .extract().response();
    }

    public static Response buscarMatriculaId(String accessToken) {
        return request()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get(MATRICULA_PATH)
            .then()
            .extract().response();
    }

    public static Response buscarDisciplinasAtuaisPortalProd(String accessToken, String matriculaId) {
        return request()
            .header("Authorization", "Bearer " + accessToken)
            .header("matriculaId", matriculaId)
        .when()
            .get(DISCIPLINAS_ATUAIS_PATH)
            .then()
            .extract().response();
    }

    public static Response buscarDetalhesMoodle(String accessToken, String courseMoodleId, String ra, String matriculaId, String educationLevel) {
        return request()
            .header("Authorization", "Bearer " + accessToken)
            .header("ra", ra)
            .header("matriculaId", matriculaId)
            .header("educationLevel", educationLevel)
        .when()
            .get(DETALHES_MOODLE_PATH + courseMoodleId)
            .then()
            .extract().response();
    }

    public static Response acessarLinkMoodle(String url) {
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
        .when()
            .post(url)
            .then()
            .extract().response();
    }

    private static RequestSpecification request() {
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
            .baseUri(ApiConfig.get().baseUri())
            .contentType("application/json");
    }

    private static String loginPayload(String login, String senha) {
        return "{\"login\":\"" + escapeJson(login) + "\",\"senha\":\"" + escapeJson(senha) + "\"}";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
