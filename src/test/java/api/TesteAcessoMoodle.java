package api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.List;
import java.util.Map;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TesteAcessoMoodle {
	private static String accessToken;
	private static String refreshToken;
	private static String matriculaId;
	private static String ra;
	private static String educationLevel;
	private static List<Map<String, Object>> listaDisciplinas;

	@Test
	@Order(1)
	public void realizaLoginPortal() {
		System.out.println("\n--- PASSO 1: REALIZANDO LOGIN ---");
		Response resposta = MetodosPublicos.realizarLoginPortalProd("esquilage199101@gmail.com", "exAhePK1dwar")
				.then()
					.statusCode(200)
					.body("token", notNullValue())
					.extract()
					.response();

		accessToken = resposta.path("token");
		refreshToken = resposta.path("refresh");
		System.out.println("Status: Login OK. Token capturado.");
	}

	@Test
	@Order(2)
	public void buscarDadosMatricula() {
		System.out.println("\n--- PASSO 2: BUSCANDO DADOS DA MATRÍCULA ---");
		Response resposta = MetodosPublicos.buscarMatriculaId(accessToken)
				.then()
					.statusCode(200)
					.extract()
					.response();

		matriculaId = resposta.jsonPath().getString("cursosPos[0].id");
		ra = resposta.jsonPath().getString("cursosPos[0].ra");
		educationLevel = resposta.jsonPath().getString("cursosPos[0].nivelEnsino");

		System.out.println("RA: " + ra);
		System.out.println("Matricula ID: " + matriculaId);
		System.out.println("Education Level: " + educationLevel);
	}

	@Test
	@Order(3)
	public void consultaDisciplinasAtuais() {
		System.out.println("\n--- PASSO 3: CONSULTANDO DISCIPLINAS ATUAIS ---");
		Response resposta = MetodosPublicos.buscarDisciplinasAtuaisPortalProd(accessToken, matriculaId)
				.then()
					.statusCode(200)
					.extract()
					.response();

		listaDisciplinas = resposta.path("content");
		System.out.println("Disciplinas encontradas: " + (listaDisciplinas != null ? listaDisciplinas.size() : 0));
	}

	@Test
	@Order(4)
	public void validarAcessoMoodle() {
		System.out.println("\n--- PASSO 4: VALIDANDO ACESSO SSO AO MOODLE ---");
		if (listaDisciplinas != null && !listaDisciplinas.isEmpty()) {
			String courseMoodleId = String.valueOf(listaDisciplinas.get(0).get("courseMoodleId"));
			System.out.println("Testando com Course Moodle ID: " + courseMoodleId);

			Response respostaDet = MetodosPublicos.buscarDetalhesMoodle(accessToken, courseMoodleId, ra, matriculaId, educationLevel)
					.then()
						.statusCode(200)
						.extract()
						.response();

			String urlAutenticada = respostaDet.asString(); 
			System.out.println("URL SSO Gerada: " + urlAutenticada);

			assertThat("URL base incorreta", urlAutenticada, containsString("https://moodleposead.unifatecie.edu.br/arq/disponivel/integra_sso.php"));
			assertThat("Parâmetro disciplina divergente", urlAutenticada, containsString("disciplina=" + courseMoodleId));
			assertThat("Parâmetro matricula divergente", urlAutenticada, containsString("matricula=" + ra));

			System.out.println("Validando conexão final (POST)...");
			MetodosPublicos.acessarLinkMoodle(urlAutenticada)
					.then()
						.statusCode(200);
			
			System.out.println("Sucesso: Moodle respondeu 200 OK.");
		} else {
			System.out.println("Falha: Lista de disciplinas vazia. Não foi possível validar o Passo 4.");
		}
	}
}
