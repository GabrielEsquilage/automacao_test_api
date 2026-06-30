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
                  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                  <style>
                    :root {
                      color-scheme: dark;
                      --bg: #0b0f19;
                      --panel: rgba(30, 41, 59, 0.4);
                      --panel-hover: rgba(30, 41, 59, 0.7);
                      --border: rgba(255, 255, 255, 0.08);
                      --text: #f8fafc;
                      --muted: #94a3b8;
                      --line: rgba(255, 255, 255, 0.05);
                      --passed: #10b981;
                      --passed-bg: rgba(16, 185, 129, 0.15);
                      --failed: #ef4444;
                      --failed-bg: rgba(239, 68, 68, 0.15);
                      --skipped: #f59e0b;
                      --skipped-bg: rgba(245, 158, 11, 0.15);
                      --primary: #3b82f6;
                      --primary-bg: rgba(59, 130, 246, 0.15);
                      --glow: 0 0 20px rgba(59, 130, 246, 0.15);
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      background: var(--bg);
                      background-image: 
                        radial-gradient(at 0% 0%, rgba(59, 130, 246, 0.15) 0px, transparent 50%),
                        radial-gradient(at 100% 100%, rgba(16, 185, 129, 0.1) 0px, transparent 50%);
                      background-attachment: fixed;
                      color: var(--text);
                      font: 14px/1.6 'Inter', sans-serif;
                      -webkit-font-smoothing: antialiased;
                    }
                    header, main { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
                    header { border-bottom: 1px solid var(--border); margin-bottom: 24px; padding-bottom: 24px; }
                    h1 { margin: 0 0 12px; font-size: 32px; font-weight: 700; letter-spacing: -0.02em; background: linear-gradient(to right, #fff, #94a3b8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
                    h2 { margin: 36px 0 16px; font-size: 22px; font-weight: 600; letter-spacing: -0.01em; }
                    .meta { color: var(--muted); display: flex; flex-wrap: wrap; gap: 12px 24px; font-size: 13px; }
                    .meta span { display: flex; align-items: center; gap: 6px; }
                    .stats {
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                      gap: 16px;
                      margin: 24px 0;
                    }
                    .stat, details, table {
                      background: var(--panel);
                      border: 1px solid var(--border);
                      border-radius: 12px;
                      backdrop-filter: blur(12px);
                      -webkit-backdrop-filter: blur(12px);
                      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                      transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
                    }
                    .stat:hover {
                      transform: translateY(-2px);
                      box-shadow: var(--glow);
                      background: var(--panel-hover);
                    }
                    .stat { padding: 20px; }
                    .stat strong { display: block; font-size: 32px; font-weight: 700; line-height: 1.2; margin-bottom: 4px; color: #fff; }
                    .stat span { color: var(--muted); font-size: 13px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; }
                    table { width: 100%; border-collapse: collapse; overflow: hidden; }
                    th, td { padding: 14px 16px; border-bottom: 1px solid var(--border); text-align: left; vertical-align: middle; }
                    th { background: rgba(0, 0, 0, 0.2); color: var(--muted); font-size: 12px; text-transform: uppercase; font-weight: 600; letter-spacing: 0.05em; }
                    tr:last-child td { border-bottom: 0; }
                    tr:hover td { background: rgba(255, 255, 255, 0.02); }
                    .status {
                      display: inline-flex;
                      align-items: center;
                      justify-content: center;
                      min-width: 80px;
                      border-radius: 6px;
                      padding: 4px 10px;
                      font-size: 11px;
                      font-weight: 700;
                      text-transform: uppercase;
                      letter-spacing: 0.05em;
                    }
                    .PASSED { background: var(--passed-bg); color: var(--passed); border: 1px solid rgba(16, 185, 129, 0.2); }
                    .FAILED { background: var(--failed-bg); color: var(--failed); border: 1px solid rgba(239, 68, 68, 0.2); box-shadow: 0 0 10px rgba(239, 68, 68, 0.1); }
                    .SKIPPED { background: var(--skipped-bg); color: var(--skipped); border: 1px solid rgba(245, 158, 11, 0.2); }
                    .tag {
                      display: inline-block;
                      margin: 2px 4px 2px 0;
                      padding: 3px 8px;
                      border: 1px solid var(--border);
                      border-radius: 6px;
                      color: var(--muted);
                      background: rgba(0, 0, 0, 0.2);
                      font-size: 11px;
                      font-weight: 500;
                    }
                    details { margin: 16px 0; overflow: hidden; }
                    summary {
                      cursor: pointer;
                      padding: 16px 20px;
                      font-weight: 600;
                      display: flex;
                      align-items: center;
                      gap: 12px;
                      background: rgba(0, 0, 0, 0.1);
                      transition: background 0.2s ease;
                    }
                    summary:hover { background: rgba(255, 255, 255, 0.03); }
                    summary::-webkit-details-marker { display: none; }
                    .detail-body { border-top: 1px solid var(--border); padding: 20px; background: rgba(0, 0, 0, 0.15); }
                    .call {
                      border: 1px solid var(--border);
                      border-radius: 10px;
                      margin: 16px 0;
                      overflow: hidden;
                      background: rgba(15, 23, 42, 0.6);
                    }
                    .call-title {
                      display: flex;
                      flex-wrap: wrap;
                      gap: 12px;
                      align-items: center;
                      padding: 12px 16px;
                      background: rgba(0, 0, 0, 0.2);
                      border-bottom: 1px solid var(--border);
                      font-family: monospace;
                      font-size: 13px;
                    }
                    .method {
                      color: var(--primary);
                      background: var(--primary-bg);
                      border: 1px solid rgba(59, 130, 246, 0.2);
                      border-radius: 6px;
                      padding: 3px 8px;
                      font-size: 11px;
                      font-weight: 700;
                      text-transform: uppercase;
                    }
                    .url { overflow-wrap: anywhere; color: #e2e8f0; flex: 1; }
                    .two-columns {
                      display: grid;
                      grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
                      gap: 16px;
                      padding: 16px;
                    }
                    pre {
                      margin: 8px 0 0;
                      padding: 14px;
                      background: #0f172a;
                      color: #cbd5e1;
                      border: 1px solid var(--border);
                      border-radius: 8px;
                      overflow: auto;
                      max-height: 400px;
                      white-space: pre-wrap;
                      word-break: break-word;
                      font-family: monospace;
                      font-size: 12px;
                      line-height: 1.5;
                    }
                    pre::-webkit-scrollbar { width: 8px; height: 8px; }
                    pre::-webkit-scrollbar-track { background: transparent; }
                    pre::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 4px; }
                    pre::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.2); }
                    .empty { color: var(--muted); font-style: italic; }
                    .error { color: #fca5a5; background: rgba(239, 68, 68, 0.1); padding: 12px 16px; border-left: 4px solid var(--failed); border-radius: 0 8px 8px 0; margin-bottom: 16px; font-family: monospace; font-size: 13px; }
                    @media (max-width: 760px) {
                      header, main { padding: 20px 16px; }
                      .two-columns { grid-template-columns: 1fr; }
                      .stats { grid-template-columns: 1fr 1fr; }
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
