package api;

//import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

//import io.restassured.http.ContentType;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TesteLoginApi {

	private static String accessToken;
	private static String refreshToken;

	@Test
	@Order(1)
	public void realizarLoginEValidarTokensNoHeader() {
		Response resposta = MetodosPublicos.realizarLoginDev("admin", "7Y/6p0p\\iYd{")
		.then()
			.statusCode(200)
			.header("access_token", notNullValue())
			.header("access_token", containsString("eyJ"))
			.header("refresh_token", notNullValue())
			.header("refresh_token", containsString("eyJ"))
			.extract()
			.response();

		accessToken = resposta.header("access_token");
		refreshToken = resposta.header("refresh_token");

		System.out.println("Access Token: " + accessToken);
		System.out.println("Refresh Token: " + refreshToken);
	}

	@Test
	@Order(2)
	public void realizarRefreshDeToken() {
		MetodosPublicos.realizarRefreshTokenDev(refreshToken)
		.then()
			.statusCode(200)
			.header("access_token", notNullValue())
			.header("access_token", containsString("eyJ"))
			.header("refresh_token", notNullValue())
			.header("refresh_token", containsString("eyJ"));
	}

	@Test
	@Order(3)
	public void realizaLoginComDadosInvalidos() {
		MetodosPublicos.realizarLoginDev("admin*", "7Y//6p0p\\iYd{*")
		.then()
			.statusCode(403)
			.body("type", is("about:blank"))
			.body("title", is("Forbidden"))
			.body("status", is(403))
			.body("detail", is("Login falhou."))
			.body("instance", containsString("/login"))
			.body("properties", is(nullValue()));
	}
}