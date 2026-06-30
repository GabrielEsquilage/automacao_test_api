package api.moodle;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.MoodleApiClient;

@Tag("api")
@Tag("moodle")
@Tag("custom_scripts")
@DisplayName("Moodle - Scripts Customizados (arq/disponivel/*.php)")
@ExtendWith(ApiReportExtension.class)
public class MoodleCustomScriptsTest {

    @Test
    @DisplayName("Teste Negativo: Acesso ao integra_sso.php sem parâmetros (GET)")
    void testIntegraSsoMissingParams() {
        MoodleApiClient.request()
            .when()
                .get("/arq/disponivel/integra_sso.php")
            .then()
                .statusCode(200)
                // Retorna um HTML informando acesso restrito
                .body(containsString("Acesso restrito!"));
    }

    @Test
    @DisplayName("Teste Negativo: Acesso ao atualiza_status_disciplina.php sem parâmetros (POST)")
    void testAtualizaStatusDisciplinaMissingParams() {
        MoodleApiClient.request()
            .when()
                .post("/arq/disponivel/atualiza_status_disciplina.php")
            .then()
                .statusCode(200)
                // Retorna um JSON validando a falta de parametros
                .body("error", equalTo("Parâmetros incompletos."));
    }

    @Test
    @DisplayName("Teste Negativo: Acesso ao retorna_agenda_url.php com token padrao (GET)")
    void testRetornaAgendaUrlMissingParams() {
        MoodleApiClient.request()
            .when()
                .get("/arq/disponivel/retorna_agenda_url.php")
            .then()
                .statusCode(200)
                // O script rejeita o wstoken do Moodle ou requer outro parametro de seguranca
                .body(containsString("Token de seguran\\u00e7a inv\\u00e1lido."))
                .body(containsString("\"status\":\"erro\""));
    }
}
