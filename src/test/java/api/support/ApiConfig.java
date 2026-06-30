package api.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public final class ApiConfig {
    private static final String DEFAULT_ENVIRONMENT = "local";
    private static final ApiConfig INSTANCE = load();

    private final Properties properties;

    private ApiConfig(Properties properties) {
        this.properties = properties;
    }

    public static ApiConfig get() {
        return INSTANCE;
    }

    public String environment() {
        return value("api.environment", "API_ENVIRONMENT").orElse(DEFAULT_ENVIRONMENT);
    }

    public String baseUri() {
        return value("api.baseUri", "API_BASE_URI")
                .orElseThrow(() -> new IllegalStateException(
                        "Configure api.baseUri, API_BASE_URI ou src/test/resources/environments/"
                                + environment() + ".properties"));
    }



    public Optional<String> portalLogin() {
        return value("portal.login", "PORTAL_LOGIN");
    }

    public Optional<String> portalSenha() {
        return value("portal.senha", "PORTAL_SENHA")
                .or(() -> value("portal.password", "PORTAL_PASSWORD"));
    }

    public boolean hasPortalCredentials() {
        return portalLogin().isPresent() && portalSenha().isPresent();
    }

    public Optional<String> erpLogin() {
        return value("login", "LOGIN");
    }

    public Optional<String> erpSenha() {
        return value("senha", "SENHA");
    }

    public boolean hasErpCredentials() {
        return erpLogin().isPresent() && erpSenha().isPresent();
    }

    public String moodleBaseUrl() {
        return value("moodle.baseUrl", "MOODLE_BASE_URL")
                .orElseThrow(() -> new IllegalStateException(
                        "Configure moodle.baseUrl, MOODLE_BASE_URL ou .env"));
    }

    public String moodleToken() {
        return value("moodle.token", "MOODLE_TOKEN")
                .orElseThrow(() -> new IllegalStateException(
                        "Configure moodle.token, MOODLE_TOKEN ou .env"));
    }

    public int reportMaxBodyChars() {
        return value("api.report.maxBodyChars", "API_REPORT_MAX_BODY_CHARS")
                .map(ApiConfig::parsePositiveInt)
                .orElse(6000);
    }

    private static ApiConfig load() {
        Properties dotEnv = loadDotEnv();
        String environment = firstNonBlank(
                System.getProperty("api.environment"),
                System.getenv("API_ENVIRONMENT"),
                dotEnv.getProperty("API_ENVIRONMENT"),
                dotEnv.getProperty("api.environment"),
                DEFAULT_ENVIRONMENT);

        Properties properties = new Properties();
        String resource = "environments/" + environment + ".properties";
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel ler " + resource, exception);
        }

        properties.putAll(dotEnv);
        properties.setProperty("api.environment", environment);
        return new ApiConfig(properties);
    }

    private Optional<String> value(String propertyName, String environmentName) {
        return Optional.ofNullable(firstNonBlank(
                System.getProperty(propertyName),
                System.getenv(environmentName),
                properties.getProperty(environmentName),
                properties.getProperty(propertyName)));
    }

    private static Properties loadDotEnv() {
        Properties dotEnv = new Properties();
        Path dotEnvPath = Path.of(".env");
        if (!Files.isRegularFile(dotEnvPath)) {
            return dotEnv;
        }

        try {
            for (String line : Files.readAllLines(dotEnvPath, StandardCharsets.UTF_8)) {
                addDotEnvLine(dotEnv, line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo .env", exception);
        }

        return dotEnv;
    }

    private static void addDotEnvLine(Properties dotEnv, String line) {
        String normalized = line.trim();
        if (normalized.isEmpty() || normalized.startsWith("#")) {
            return;
        }
        if (normalized.startsWith("export ")) {
            normalized = normalized.substring("export ".length()).trim();
        }

        int separator = normalized.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = normalized.substring(0, separator).trim();
        String value = normalized.substring(separator + 1).trim();
        dotEnv.setProperty(key, unquote(value));
    }

    private static String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static int parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : 6000;
        } catch (NumberFormatException exception) {
            return 6000;
        }
    }
}
