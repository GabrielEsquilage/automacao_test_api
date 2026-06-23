package api.support;

import java.util.concurrent.TimeUnit;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public final class ApiReportFilter implements Filter {
    public static final ApiReportFilter INSTANCE = new ApiReportFilter();

    private ApiReportFilter() {
    }

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext context) {
        long startedAt = System.nanoTime();
        Response response = null;
        Throwable failure = null;

        try {
            response = context.next(requestSpec, responseSpec);
            return response;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            ApiReport.recordCall(requestSpec, response, durationMs, failure);
        }
    }
}
