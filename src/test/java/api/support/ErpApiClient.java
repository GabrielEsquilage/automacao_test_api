package api.support;

import api.auth.ErpLoginApi;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assumptions;

public class ErpApiClient {

    private static String accessToken;

    public static synchronized String getAccessToken() {
        if (accessToken == null) {
            ApiConfig config = ApiConfig.get();
            Assumptions.assumeTrue(config.hasErpCredentials(), "Credenciais ERP nao encontradas no .env");
            Response response = ErpLoginApi.realizarLogin(
                config.erpLogin().get(), 
                config.erpSenha().get()
            );
            
            if (response.statusCode() == 200) {
                accessToken = response.header("access_token");
                if (accessToken == null) {
                    throw new RuntimeException("Login falhou em retornar o access_token no header");
                }
            } else {
                throw new RuntimeException("Falha ao autenticar no ERP para testes integrados. Status: " + response.statusCode() + " Body: " + response.getBody().asString());
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
