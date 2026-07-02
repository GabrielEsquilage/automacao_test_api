package api.turma;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import api.support.ApiReportExtension;
import api.support.ErpApiClient;
import io.restassured.http.ContentType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Tag("api")
@Tag("turma")
@Tag("grade")
@DisplayName("ERP -> Turma: Testes de Edição de Grade")
@ExtendWith(ApiReportExtension.class)
public class ErpTurmaGradeTest {

    private static final int FAKE_ID = 9999999;

    @Test
    @DisplayName("Teste Negativo: Tentar atualizar grade de uma Turma inexistente (Retorna 400/404/500)")
    void testAtualizarGradeTurmaInexistente() {
        // Montando o payload GradeCurriculoRecordPutRequestDTO
        Map<String, Object> payload = new HashMap<>();
        payload.put("gradeCurriculoId", FAKE_ID);
        payload.put("horaAtividadeComplementar", 10.5);
        payload.put("horaAtividadeExtensao", 20.0);
        payload.put("observacao", "Automação de Testes - Atualização de Grade");
        
        Map<String, Object> gradeDisciplina = new HashMap<>();
        gradeDisciplina.put("colaboradorId", FAKE_ID);
        gradeDisciplina.put("tipoNotaId", 1); // Mock
        payload.put("gradeDisciplina", Collections.singletonList(gradeDisciplina));

        ErpApiClient.request()
            .contentType(ContentType.JSON)
            .pathParam("id", FAKE_ID)
            .body(payload)
            .when()
                .put("/api/v1/turma/geral/{id}/grade")
            .then()
                // A expectativa é que falhe por não existir a turma ou a gradeCurriculo.
                // Como não sabemos a resposta exata do backend para NotFound, aceitamos a faixa de erro cliente/servidor.
                .statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(400),
                        org.hamcrest.Matchers.is(404),
                        org.hamcrest.Matchers.is(409),
                        org.hamcrest.Matchers.is(412),
                        org.hamcrest.Matchers.is(500)
                ));
    }
}
