package api.auth;

import api.support.ApiConfig;
import api.support.ApiReportFilter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class PortalLoginApi {
    private static final String LOGIN_PATH = "/api-external/v1/portal/auth/login";

    public static Response realizarLogin(String login, String senha) {
        Map<String, String> body = new HashMap<>();
        body.put("login", login);
        body.put("senha", senha);

        return request()
            .log().all()
            .body(body)
        .when()
            .post(LOGIN_PATH)
        .then()
            .log().all()
            .extract().response();
    }

    private static RequestSpecification request() {
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
            .baseUri(ApiConfig.get().baseUri())
            .contentType(io.restassured.http.ContentType.JSON);
    }
}
