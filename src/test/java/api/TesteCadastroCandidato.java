package api;

import static io.restassured.RestAssured.given;
import org.junit.jupiter.api.Test;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.List;

public class TesteCadastroCandidato {

    @Test
    public void cadastrarCandidatoComSucesso() {
        var endereco = Map.of(
            "bairro", "Praia de Iracema",
            "cep", "68309-584",
            "cidadeId", 10677,
            "logradouro", "Avenida Santo Amaro",
            "numero", "2291",
            "ufId", 26
        );

        var pessoa = Map.of(
            "cpf", "572.001.942-12",
            "email", "roberto.lopes.freitas.novo.1130@emailteste.com",
            "endereco", endereco,
            "generoId", 3,
            "nascimento", "1998-06-17",
            "nome", "Roberto Lopes Freitas",
            "racaId", 10,
            "telefone", "(21) 97605-9145"
        );

        var body = Map.of(
            "concursoCurriculoId", 1076,
            "concursoCurriculoPlanoPagamentoId", 3220,
            "concursoFilialId", 14730,
            "diaVencimento", 10,
            "formaPagamentoId", 3,
            "pessoa", pessoa,
            "termoIds", List.of(3, 5)
        );

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("https://erp-api-dev-922117522963.us-central1.run.app/api-external/v1/aluno-pos/create-student")
        .then()
            .statusCode(201);
    }
}
