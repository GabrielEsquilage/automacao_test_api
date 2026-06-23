package api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import api.support.ApiConfig;
import api.support.ApiReportExtension;
import io.restassured.response.Response;

@Tag("api")
@Tag("moodle")
@DisplayName("Portal Moodle - acesso SSO")
@ExtendWith(ApiReportExtension.class)
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
	@DisplayName("Realiza login no portal")
	public void realizaLoginPortal() {
		ApiConfig config = ApiConfig.get();
		assumeTrue(config.hasPortalCredentials(), "Defina PORTAL_LOGIN e PORTAL_SENHA para executar este fluxo.");

		System.out.println("\n--- PASSO 1: REALIZANDO LOGIN ---");
		Response resposta = MetodosPublicos.realizarLoginPortalProd(
				config.portalLogin().orElseThrow(),
				config.portalSenha().orElseThrow())
				.then()
					.statusCode(200)
					.body("token", notNullValue())
					.body("refresh", notNullValue())
					.extract()
					.response();

		accessToken = resposta.path("token");
		refreshToken = resposta.path("refresh");
		System.out.println("Status: Login OK. Token capturado.");
	}

	@Test
	@Order(2)
	@DisplayName("Busca dados da matricula")
	public void buscarDadosMatricula() {
		assumeTrue(accessToken != null, "Login nao foi executado com sucesso.");

		System.out.println("\n--- PASSO 2: BUSCANDO DADOS DA MATRÍCULA ---");
		Response resposta = MetodosPublicos.buscarMatriculaId(accessToken)
				.then()
					.statusCode(200)
					.extract()
					.response();

		matriculaId = resposta.jsonPath().getString("cursosPos[0].id");
		ra = resposta.jsonPath().getString("cursosPos[0].ra");
		educationLevel = resposta.jsonPath().getString("cursosPos[0].nivelEnsino");

		assertThat("Matricula ID nao retornada", matriculaId, notNullValue());
		assertThat("RA nao retornado", ra, notNullValue());
		assertThat("Nivel de ensino nao retornado", educationLevel, notNullValue());

		System.out.println("RA: " + ra);
		System.out.println("Matricula ID: " + matriculaId);
		System.out.println("Education Level: " + educationLevel);
	}

	@Test
	@Order(3)
	@DisplayName("Consulta disciplinas atuais")
	public void consultaDisciplinasAtuais() {
		assumeTrue(accessToken != null && matriculaId != null, "Dados de login/matricula nao foram carregados.");

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
	@DisplayName("Valida acesso SSO ao Moodle")
	public void validarAcessoMoodle() {
		assumeTrue(accessToken != null && matriculaId != null && ra != null && educationLevel != null,
				"Dados obrigatorios do fluxo nao foram carregados.");
		assumeTrue(listaDisciplinas != null && !listaDisciplinas.isEmpty(),
				"Lista de disciplinas vazia. Nao foi possivel validar o acesso ao Moodle.");

		System.out.println("\n--- PASSO 4: VALIDANDO ACESSO SSO AO MOODLE ---");
		List<Executable> validacoesDisciplinas = new ArrayList<>();

		for (int indice = 0; indice < listaDisciplinas.size(); indice++) {
			int numeroDisciplina = indice + 1;
			Map<String, Object> disciplina = listaDisciplinas.get(indice);
			Object courseMoodleId = disciplina.get("courseMoodleId");

			validacoesDisciplinas.add(() -> {
				assertThat("Course Moodle ID nao retornado para disciplina " + numeroDisciplina, courseMoodleId, notNullValue());
				validarAcessoMoodleDisciplina(numeroDisciplina, String.valueOf(courseMoodleId));
			});
		}

		assertAll("Valida acesso SSO para todas as disciplinas atuais", validacoesDisciplinas);
	}

	private void validarAcessoMoodleDisciplina(int numeroDisciplina, String courseMoodleId) {
		System.out.println("Testando disciplina " + numeroDisciplina + " com Course Moodle ID: " + courseMoodleId);

		Response respostaDet = MetodosPublicos
				.buscarDetalhesMoodle(accessToken, courseMoodleId, ra, matriculaId, educationLevel)
				.then()
					.statusCode(200)
					.extract()
					.response();

		String urlAutenticada = respostaDet.asString();
		System.out.println("URL SSO Gerada: " + urlAutenticada);

		assertThat("URL base incorreta", urlAutenticada, containsString("https://moodleposead.unifatecie.edu.br/arq/disponivel/integra_sso.php"));
		assertThat("Parametro disciplina divergente para Course Moodle ID " + courseMoodleId, urlAutenticada, containsString("disciplina=" + courseMoodleId));
		assertThat("Parametro matricula divergente", urlAutenticada, containsString("matricula=" + ra));

		System.out.println("Validando conexão final (POST)...");
		Response respostaMoodle = MetodosPublicos.acessarLinkMoodle(urlAutenticada)
				.then()
					.statusCode(200)
					.extract()
					.response();

		assertThat(
				"Moodle retornou acesso restrito por token invalido para Course Moodle ID " + courseMoodleId,
				respostaMoodle.asString(),
				not(containsString("Acesso restrito: Token inválido!")));

		System.out.println("Sucesso: Moodle respondeu 200 OK para Course Moodle ID " + courseMoodleId + ".");
	}
}
