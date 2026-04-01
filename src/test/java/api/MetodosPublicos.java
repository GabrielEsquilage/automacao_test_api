package api;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

public class MetodosPublicos {

    
    public static Response realizarLoginDev(String login, String password) {
        return given()
            .formParam("login", login)
            .formParam("password", password)
        .when()
            .post("https://erp-api-stage.inovacarreira.com.br/api/v1/app/auth/login");
    }

    
    public static Response realizarRefreshTokenDev(String refreshToken) {
        return given()
            .header("Refresh-Token", "Bearer " + refreshToken)
        .when()
            .post("https://erp-api-stage.inovacarreira.com.br/api/v1/app/auth/token/refresh");
    }
    
    public static Response realizarLoginPortalProd(String login, String senha) {
        return given()
            .formParam("login", login)
            .formParam("senha", senha)
        .when()
            .post("https://erp-api-prod-964330493122.southamerica-east1.run.app/api-external/v1/portal/auth/login");
    }
    
    public static Response realizarLoginPortalDev(String login, String senha) {
        return given()
            .formParam("login", login)
            .formParam("senha", senha)
        .when()
            .post("https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/portal/auth/login");
    }
}
