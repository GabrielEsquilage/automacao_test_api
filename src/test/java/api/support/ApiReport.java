package api.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;

final class ApiReport {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    private static final List<TestExecution> TESTS = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<TestExecution> CURRENT_TEST = new ThreadLocal<>();

    private ApiReport() {
    }

    static void startTest(ExtensionContext context) {
        TestExecution test = new TestExecution(
                context.getUniqueId(),
                context.getRequiredTestClass().getName(),
                context.getTestMethod().map(Method::getName).orElse(context.getDisplayName()),
                context.getDisplayName(),
                context.getTags(),
                Instant.now());

        TESTS.add(test);
        CURRENT_TEST.set(test);
    }

    static void finishTest(ExtensionContext context, Throwable failure) {
        TestExecution test = CURRENT_TEST.get();
        if (test == null) {
            return;
        }

        test.durationMs = Duration.between(test.startedAt, Instant.now()).toMillis();
        if (failure == null) {
            test.status = Status.PASSED;
        } else if (failure instanceof TestAbortedException) {
            test.status = Status.SKIPPED;
            test.errorType = failure.getClass().getSimpleName();
            test.errorMessage = failure.getMessage();
        } else {
            test.status = Status.FAILED;
            test.errorType = failure.getClass().getSimpleName();
            test.errorMessage = failure.getMessage();
        }

        CURRENT_TEST.remove();
    }

    static void recordCall(
            FilterableRequestSpecification requestSpec,
            Response response,
            long durationMs,
            Throwable failure) {
        TestExecution test = CURRENT_TEST.get();
        if (test == null) {
            return;
        }

        test.calls.add(ApiCall.from(requestSpec, response, durationMs, failure));
    }

    static synchronized void writeReport() {
        Path outputDirectory = Path.of(System.getProperty("api.report.dir", "target/api-report"));
        Path outputFile = outputDirectory.resolve("index.html");

        try {
            Files.createDirectories(outputDirectory);
            Files.writeString(outputFile, renderHtml(snapshot()), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o relatorio de API em " + outputFile, exception);
        }
    }

    private static List<TestExecution> snapshot() {
        return TESTS.stream()
                .sorted(Comparator.comparing(test -> test.startedAt))
                .collect(Collectors.toList());
    }

    private static String renderHtml(List<TestExecution> tests) {
        long passed = tests.stream().filter(test -> test.status == Status.PASSED).count();
        long failed = tests.stream().filter(test -> test.status == Status.FAILED).count();
        long skipped = tests.stream().filter(test -> test.status == Status.SKIPPED).count();
        long totalDuration = tests.stream().mapToLong(test -> test.durationMs).sum();
        int totalCalls = tests.stream().mapToInt(test -> test.calls.size()).sum();

        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="pt-BR">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Relatorio de Testes de API</title>
                  <style>
                    :root {
                      color-scheme: light;
                      --bg: #f6f7f9;
                      --panel: #ffffff;
                      --text: #1d2733;
                      --muted: #64748b;
                      --line: #d9e0e8;
                      --passed: #15803d;
                      --failed: #b91c1c;
                      --skipped: #a16207;
                      --method: #155e75;
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      background: var(--bg);
                      color: var(--text);
                      font: 14px/1.5 Arial, Helvetica, sans-serif;
                    }
                    header, main { max-width: 1180px; margin: 0 auto; padding: 24px; }
                    header { padding-bottom: 8px; }
                    h1 { margin: 0 0 8px; font-size: 28px; }
                    h2 { margin: 28px 0 12px; font-size: 20px; }
                    .meta { color: var(--muted); display: flex; flex-wrap: wrap; gap: 10px 18px; }
                    .stats {
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
                      gap: 12px;
                      margin: 20px 0;
                    }
                    .stat, details, table {
                      background: var(--panel);
                      border: 1px solid var(--line);
                      border-radius: 8px;
                    }
                    .stat { padding: 14px; }
                    .stat strong { display: block; font-size: 24px; line-height: 1.1; }
                    .stat span { color: var(--muted); }
                    table { width: 100%; border-collapse: collapse; overflow: hidden; }
                    th, td { padding: 10px 12px; border-bottom: 1px solid var(--line); text-align: left; vertical-align: top; }
                    th { background: #eef2f6; color: #334155; font-size: 12px; text-transform: uppercase; }
                    tr:last-child td { border-bottom: 0; }
                    .status {
                      display: inline-block;
                      min-width: 72px;
                      border-radius: 999px;
                      padding: 3px 9px;
                      color: #fff;
                      font-size: 12px;
                      font-weight: 700;
                      text-align: center;
                    }
                    .PASSED { background: var(--passed); }
                    .FAILED { background: var(--failed); }
                    .SKIPPED { background: var(--skipped); }
                    .tag {
                      display: inline-block;
                      margin: 0 4px 4px 0;
                      padding: 2px 7px;
                      border: 1px solid var(--line);
                      border-radius: 999px;
                      color: #334155;
                      background: #f8fafc;
                      font-size: 12px;
                    }
                    details { margin: 12px 0; }
                    summary {
                      cursor: pointer;
                      padding: 13px 14px;
                      font-weight: 700;
                    }
                    .detail-body { border-top: 1px solid var(--line); padding: 14px; }
                    .call {
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      margin: 12px 0;
                      overflow: hidden;
                    }
                    .call-title {
                      display: flex;
                      flex-wrap: wrap;
                      gap: 8px;
                      align-items: center;
                      padding: 10px 12px;
                      background: #f8fafc;
                      border-bottom: 1px solid var(--line);
                    }
                    .method {
                      color: #fff;
                      background: var(--method);
                      border-radius: 5px;
                      padding: 2px 7px;
                      font-size: 12px;
                      font-weight: 700;
                    }
                    .url { overflow-wrap: anywhere; }
                    .two-columns {
                      display: grid;
                      grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
                      gap: 12px;
                      padding: 12px;
                    }
                    pre {
                      margin: 6px 0 0;
                      padding: 10px;
                      background: #111827;
                      color: #e5e7eb;
                      border-radius: 6px;
                      overflow: auto;
                      max-height: 360px;
                      white-space: pre-wrap;
                      word-break: break-word;
                    }
                    .empty { color: var(--muted); }
                    .error { color: var(--failed); font-weight: 700; }
                    @media (max-width: 760px) {
                      header, main { padding: 16px; }
                      .two-columns { grid-template-columns: 1fr; }
                      th, td { padding: 8px; }
                    }
                  </style>
                </head>
                <body>
                """);

        html.append("<header>")
                .append("<h1>Relatorio de Testes de API</h1>")
                .append("<div class=\"meta\">")
                .append("<span>Ambiente: ").append(html(ApiConfig.get().environment())).append("</span>")
                .append("<span>Base URI: ").append(html(redact(ApiConfig.get().baseUri()))).append("</span>")
                .append("<span>Gerado em: ").append(html(DISPLAY_TIME.format(Instant.now()))).append("</span>")
                .append("</div>")
                .append("</header>");

        html.append("<main>");
        html.append("<section class=\"stats\">")
                .append(stat("Total", String.valueOf(tests.size())))
                .append(stat("Passou", String.valueOf(passed)))
                .append(stat("Falhou", String.valueOf(failed)))
                .append(stat("Ignorado", String.valueOf(skipped)))
                .append(stat("Chamadas HTTP", String.valueOf(totalCalls)))
                .append(stat("Tempo", formatDuration(totalDuration)))
                .append("</section>");

        html.append("<h2>Visao Geral</h2>");
        if (tests.isEmpty()) {
            html.append("<p class=\"empty\">Nenhum teste foi registrado.</p>");
        } else {
            html.append("""
                    <table>
                      <thead>
                        <tr>
                          <th>Status</th>
                          <th>Teste</th>
                          <th>Tags</th>
                          <th>Chamadas</th>
                          <th>Tempo</th>
                        </tr>
                      </thead>
                      <tbody>
                    """);
            for (TestExecution test : tests) {
                html.append("<tr>")
                        .append("<td>").append(status(test.status)).append("</td>")
                        .append("<td>").append(html(test.className)).append("<br><strong>")
                        .append(html(test.displayName)).append("</strong></td>")
                        .append("<td>").append(tags(test.tags)).append("</td>")
                        .append("<td>").append(test.calls.size()).append("</td>")
                        .append("<td>").append(formatDuration(test.durationMs)).append("</td>")
                        .append("</tr>");
            }
            html.append("</tbody></table>");
        }

        html.append("<h2>Detalhes</h2>");
        for (TestExecution test : tests) {
            renderTestDetails(html, test);
        }

        html.append("</main></body></html>");
        return html.toString();
    }

    private static void renderTestDetails(StringBuilder html, TestExecution test) {
        html.append("<details ")
                .append(test.status == Status.FAILED ? "open" : "")
                .append("><summary>")
                .append(status(test.status)).append(" ")
                .append(html(test.displayName))
                .append(" <span class=\"empty\">")
                .append(formatDuration(test.durationMs))
                .append("</span></summary>")
                .append("<div class=\"detail-body\">")
                .append("<p><strong>Classe:</strong> ").append(html(test.className))
                .append("<br><strong>Metodo:</strong> ").append(html(test.methodName))
                .append("<br><strong>Inicio:</strong> ").append(html(DISPLAY_TIME.format(test.startedAt)))
                .append("<br><strong>Tags:</strong> ").append(tags(test.tags))
                .append("</p>");

        if (test.errorMessage != null) {
            html.append("<p class=\"error\">")
                    .append(html(test.errorType)).append(": ")
                    .append(html(test.errorMessage))
                    .append("</p>");
        }

        if (test.calls.isEmpty()) {
            html.append("<p class=\"empty\">Nenhuma chamada HTTP registrada neste teste.</p>");
        } else {
            for (ApiCall call : test.calls) {
                renderCall(html, call);
            }
        }

        html.append("</div></details>");
    }

    private static void renderCall(StringBuilder html, ApiCall call) {
        html.append("<div class=\"call\">")
                .append("<div class=\"call-title\">")
                .append("<span class=\"method\">").append(html(call.method)).append("</span>")
                .append("<span class=\"url\">").append(html(call.uri)).append("</span>")
                .append("<span>").append(call.statusCode > 0 ? call.statusCode : "erro").append("</span>")
                .append("<span>").append(formatDuration(call.durationMs)).append("</span>")
                .append("</div>");

        if (call.failureMessage != null) {
            html.append("<div class=\"detail-body\"><p class=\"error\">")
                    .append(html(call.failureMessage))
                    .append("</p></div>");
        }

        html.append("<div class=\"two-columns\">")
                .append("<div><strong>Request headers</strong><pre>")
                .append(html(emptyText(call.requestHeaders))).append("</pre></div>")
                .append("<div><strong>Response headers</strong><pre>")
                .append(html(emptyText(call.responseHeaders))).append("</pre></div>")
                .append("<div><strong>Request body</strong><pre>")
                .append(html(emptyText(call.requestBody))).append("</pre></div>")
                .append("<div><strong>Response body</strong><pre>")
                .append(html(emptyText(call.responseBody))).append("</pre></div>")
                .append("</div></div>");
    }

    private static String stat(String label, String value) {
        return "<div class=\"stat\"><strong>" + html(value) + "</strong><span>" + html(label) + "</span></div>";
    }

    private static String status(Status status) {
        return "<span class=\"status " + status.name() + "\">" + status.label + "</span>";
    }

    private static String tags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "<span class=\"empty\">sem tags</span>";
        }
        return tags.stream()
                .sorted()
                .map(tag -> "<span class=\"tag\">" + html(tag) + "</span>")
                .collect(Collectors.joining());
    }

    private static String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + " ms";
        }
        return String.format(Locale.ROOT, "%.2f s", durationMs / 1000.0);
    }

    private static String emptyText(String value) {
        return value == null || value.isBlank() ? "vazio" : value;
    }

    private static String html(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (char character : value.toCharArray()) {
            switch (character) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private static String headers(Headers headers) {
        if (headers == null) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        for (Header header : headers) {
            String value = isSensitive(header.getName()) ? "***" : redact(header.getValue());
            lines.add(header.getName() + ": " + value);
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static boolean isSensitive(String name) {
        if (name == null) {
            return false;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("authorization")
                || normalized.contains("token")
                || normalized.contains("cookie")
                || normalized.contains("senha")
                || normalized.contains("password");
    }

    private static String requestBody(FilterableRequestSpecification requestSpec) {
        Object body = requestSpec.getBody();
        if (body == null) {
            return "";
        }
        if (body instanceof byte[] bytes) {
            return "[binary body: " + bytes.length + " bytes]";
        }
        return limit(redact(String.valueOf(body)));
    }

    private static String responseBody(Response response) {
        if (response == null) {
            return "";
        }
        try {
            return limit(redact(response.asString()));
        } catch (RuntimeException exception) {
            return "[response body indisponivel: " + exception.getClass().getSimpleName() + "]";
        }
    }

    private static String redact(String value) {
        if (value == null) {
            return "";
        }

        String redacted = value
                .replaceAll("(?i)(\\\"(?:token|refresh|accessToken|senha|password)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")", "$1***$2")
                .replaceAll("(?i)((?:token|refresh|accessToken|senha|password)=)[^&\\s]+", "$1***")
                .replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+", "$1***");
        return redacted;
    }

    private static String limit(String value) {
        int maxChars = ApiConfig.get().reportMaxBodyChars();
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + System.lineSeparator() + "... [conteudo truncado]";
    }

    private enum Status {
        PASSED("PASSOU"),
        FAILED("FALHOU"),
        SKIPPED("IGNORADO");

        private final String label;

        Status(String label) {
            this.label = label;
        }
    }

    private static final class TestExecution {
        private final String id;
        private final String className;
        private final String methodName;
        private final String displayName;
        private final Set<String> tags;
        private final Instant startedAt;
        private final List<ApiCall> calls = Collections.synchronizedList(new ArrayList<>());
        private Status status = Status.SKIPPED;
        private long durationMs;
        private String errorType;
        private String errorMessage;

        private TestExecution(
                String id,
                String className,
                String methodName,
                String displayName,
                Set<String> tags,
                Instant startedAt) {
            this.id = id;
            this.className = className;
            this.methodName = methodName;
            this.displayName = displayName;
            this.tags = Set.copyOf(tags);
            this.startedAt = startedAt;
        }
    }

    private static final class ApiCall {
        private final String method;
        private final String uri;
        private final int statusCode;
        private final long durationMs;
        private final String requestHeaders;
        private final String requestBody;
        private final String responseHeaders;
        private final String responseBody;
        private final String failureMessage;

        private ApiCall(
                String method,
                String uri,
                int statusCode,
                long durationMs,
                String requestHeaders,
                String requestBody,
                String responseHeaders,
                String responseBody,
                String failureMessage) {
            this.method = method;
            this.uri = uri;
            this.statusCode = statusCode;
            this.durationMs = durationMs;
            this.requestHeaders = requestHeaders;
            this.requestBody = requestBody;
            this.responseHeaders = responseHeaders;
            this.responseBody = responseBody;
            this.failureMessage = failureMessage;
        }

        private static ApiCall from(
                FilterableRequestSpecification requestSpec,
                Response response,
                long durationMs,
                Throwable failure) {
            return new ApiCall(
                    requestSpec.getMethod(),
                    redact(requestSpec.getURI()),
                    response == null ? -1 : response.getStatusCode(),
                    durationMs,
                    headers(requestSpec.getHeaders()),
                    requestBody(requestSpec),
                    response == null ? "" : headers(response.getHeaders()),
                    responseBody(response),
                    failure == null ? null : failure.getClass().getSimpleName() + ": " + failure.getMessage());
        }
    }
}
