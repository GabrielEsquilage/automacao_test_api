package api.support;

import api.auth.PortalLoginApi;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assumptions;

public class PortalApiClient {

    private static String accessToken;

    public static synchronized String getAccessToken() {
        if (accessToken == null) {
            ApiConfig config = ApiConfig.get();
            Assumptions.assumeTrue(config.hasPortalCredentials(), "Credenciais PORTAL nao encontradas no .env");
            Response response = PortalLoginApi.realizarLogin(
                config.portalLogin().get(), 
                config.portalSenha().get()
            );
            
            if (response.statusCode() == 200) {
                accessToken = response.jsonPath().getString("token");
                if (accessToken == null) {
                    throw new RuntimeException("Login falhou em retornar o token no body");
                }
            } else {
                throw new RuntimeException("Falha ao autenticar no Portal. Status: " + response.statusCode() + " Body: " + response.getBody().asString());
            }
        }
        return accessToken;
    }

    public static RequestSpecification request() {
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
            .baseUri(ApiConfig.get().baseUri())
            .header("Authorization", "Bearer " + getAccessToken())
            .contentType("application/json");
    }
}
