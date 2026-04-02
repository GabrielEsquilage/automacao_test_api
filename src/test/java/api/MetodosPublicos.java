package api;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class MetodosPublicos {

    public static Response realizarLoginPortalProd(String login, String senha) {
        String corpoJson = "{\"login\": \"" + login + "\", \"senha\": \"" + senha + "\"}";
        
        return given()
            .contentType("application/json")
            .body(corpoJson)
        .when()
            .post("https://erp-api-prod-964330493122.southamerica-east1.run.app/api-external/v1/portal/auth/login")
            .then()
            .extract().response();
    }

    public static Response buscarMatriculaId(String accessToken) {
        return given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType("application/json")
        .when()
            .get("https://erp-api-prod-964330493122.southamerica-east1.run.app/api-external/v1/portal/matricula/matricula-disciplina")
            .then()
            .extract().response();
    }

    public static Response buscarDisciplinasAtuaisPortalProd(String accessToken, String matriculaId) {
        return given()
            .header("Authorization", "Bearer " + accessToken)
            .header("matriculaId", matriculaId)
            .contentType("application/json")
        .when()
            .get("https://erp-api-prod-964330493122.southamerica-east1.run.app/api-external/v1/portal/moodle/disciplinas-current")
            .then()
            .extract().response();
    }

    public static Response buscarDetalhesMoodle(String accessToken, String courseMoodleId, String ra, String matriculaId, String educationLevel) {
        return given()
            .header("Authorization", "Bearer " + accessToken)
            .header("ra", ra)
            .header("matriculaId", matriculaId)
            .header("educationLevel", educationLevel)
            .contentType("application/json")
        .when()
            .get("https://erp-api-prod-964330493122.southamerica-east1.run.app/api-external/v1/portal/moodle/disciplina-link/" + courseMoodleId)
            .then()
            .extract().response();
    }

    public static Response acessarLinkMoodle(String url) {
        return given()
        .when()
            .post(url)
            .then()
            .extract().response();
    }
}
