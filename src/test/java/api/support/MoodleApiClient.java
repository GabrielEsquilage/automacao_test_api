package api.support;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

public class MoodleApiClient {

    public static RequestSpecification request() {
        ApiConfig config = ApiConfig.get();
        return RestAssured.given()
            .filter(ApiReportFilter.INSTANCE)
            .filter(new ResponseTimeLoggingFilter()) // Filtro customizado para medir tempo no console/log
            .baseUri(config.moodleBaseUrl())
            .queryParam("wstoken", config.moodleToken())
            .queryParam("moodlewsrestformat", "json")
            .contentType("application/x-www-form-urlencoded"); // Padrão Moodle para POST
    }

    private static class ResponseTimeLoggingFilter implements Filter {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            long start = System.currentTimeMillis();
            Response response = ctx.next(requestSpec, responseSpec);
            long timeInMs = System.currentTimeMillis() - start;
            
            String function = requestSpec.getQueryParams().get("wsfunction");
            if (function == null) {
                function = requestSpec.getURI(); // fallback para scripts em /arq/disponivel
            }
            
            System.out.println("⏱️ [MOODLE PERF] Função/Rota: " + function + " -> Tempo de Resposta: " + timeInMs + " ms");
            
            return response;
        }
    }
}
