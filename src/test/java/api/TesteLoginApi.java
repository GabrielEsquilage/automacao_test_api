package api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;


public class TesteLoginApi {
	
	@Test
	public void realizarLoginEValidarTokensNoHeader() {
		Response resposta = given()
			.formParam("login", "admin")
			.formParam("password", "7Y/6p0p\\iYd{")
		.when()
			.post("https://erp-api-stage.inovacarreira.com.br/api/v1/app/auth/login")
		.then()
			.statusCode(200)
			.header("access_token", notNullValue())
			.header("access_token", containsString("eyJ"))
			.header("refresh_token", notNullValue())
			.header("refresh_token", containsString("eyJ"))
			.extract()
			.response();
		
		String accessToken = resposta.header("access_token");
        String refreshToken = resposta.header("refresh_token");

        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
			
	}
}
