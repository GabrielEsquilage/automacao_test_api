package api.auth;

import api.support.ApiConfig;
import api.support.ApiReportFilter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

public class ErpRefreshApi {
    private static final String REFRESH_PATH = "/api/v1/app/auth/token/refresh";

    public static Response atualizarToken(String refreshToken) {
        return request()
            .log().all()
            .header("Refresh-Token", "Bearer " + refreshToken)
        .when()
            .post(REFRESH_PATH)
        .then()
            .log().all()
            .extract().response();
    }

    private static RequestSpecification request() {
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
            .baseUri(ApiConfig.get().baseUri())
            .contentType("application/json");
    }
}
