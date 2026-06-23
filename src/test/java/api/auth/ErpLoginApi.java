package api.auth;

import api.support.ApiConfig;
import api.support.ApiReportFilter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class ErpLoginApi {
    private static final String LOGIN_PATH = "/api/v1/app/auth/login";

    public static Response realizarLogin(String login, String senha) {
        return request()
            .log().all()
            .formParam("login", login)
            .formParam("password", senha)
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
            .contentType(io.restassured.http.ContentType.URLENC);
    }
}
