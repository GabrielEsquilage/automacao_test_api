package api.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiConfig;
import api.support.ApiReportExtension;
import io.restassured.response.Response;

@Tag("api")
@Tag("erp")
@DisplayName("ERP - Testes de Autenticacao")
@ExtendWith(ApiReportExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ErpAuthTest {

    private static String currentRefreshToken;

    @Test
    @Order(1)
    @DisplayName("Deve realizar o login com sucesso")
    public void testLoginSucesso() {
        ApiConfig config = ApiConfig.get();
        assumeTrue(config.hasErpCredentials(), "Credenciais ERP (LOGIN/SENHA) nao encontradas no .env");

        String login = config.erpLogin().orElseThrow();
        String senha = config.erpSenha().orElseThrow();
        
        System.out.println("DEBUG LOGIN LIDO: [" + login + "]");
        System.out.println("DEBUG SENHA LIDA: [" + senha + "]");

        Response response = ErpLoginApi.realizarLogin(login, senha)
            .then()
                .statusCode(200)
                .extract().response();
        
        String accessToken = response.header("access_token");
        String refreshToken = response.header("refresh_token");

        org.junit.jupiter.api.Assertions.assertNotNull(accessToken, "access_token no header");
        org.junit.jupiter.api.Assertions.assertNotNull(refreshToken, "refresh_token no header");

        System.out.println("ACCESS TOKEN: " + accessToken);
        System.out.println("REFRESH TOKEN: " + refreshToken);
        
        
        currentRefreshToken = refreshToken;
        System.out.println("Login efetuado! Refresh Token capturado: " + currentRefreshToken);
    }

    @Test
    @Order(2)
    @DisplayName("Deve atualizar o token com sucesso usando o refresh token")
    public void testRefreshTokenSucesso() {
        assumeTrue(currentRefreshToken != null, "O teste de login falhou ou nao gerou o refresh token.");

        Response response = ErpRefreshApi.atualizarToken(currentRefreshToken)
            .then()
                .statusCode(200)
                .extract().response();

        String newAccessToken = response.header("access_token");
        String newRefreshToken = response.header("refresh_token");

        org.junit.jupiter.api.Assertions.assertNotNull(newAccessToken, "Novo access_token no header");
        org.junit.jupiter.api.Assertions.assertNotNull(newRefreshToken, "Novo refresh_token no header");
        
        System.out.println("Token atualizado com sucesso via Refresh Token!");
    }
}
