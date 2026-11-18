package ifmo.poster.contracts.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Образ PostgreSQL для Testcontainers — только из конфига, без хардкода в тестах.
 * <p>
 * Приоритет: system property → env → classpath {@code testcontainers.properties}.
 */
public final class PostgresTestImage {

    public static final String SYSTEM_PROPERTY = "testcontainers.postgresql.image";
    public static final String ENV_VARIABLE = "POSTGRES_IMAGE";
    private static final String CLASSPATH_RESOURCE = "/testcontainers.properties";
    private static final String RESOURCE_KEY = "postgresql.image";

    private PostgresTestImage() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String resolve() {
        String fromSystem = System.getProperty(SYSTEM_PROPERTY);
        if (isPresent(fromSystem)) {
            return fromSystem.trim();
        }

        String fromEnv = System.getenv(ENV_VARIABLE);
        if (isPresent(fromEnv)) {
            return fromEnv.trim();
        }

        try (InputStream in = PostgresTestImage.class.getResourceAsStream(CLASSPATH_RESOURCE)) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                String fromClasspath = properties.getProperty(RESOURCE_KEY);
                if (isPresent(fromClasspath)) {
                    return fromClasspath.trim();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось прочитать " + CLASSPATH_RESOURCE, ex);
        }

        throw new IllegalStateException(
                "Не задан образ PostgreSQL для Testcontainers. Укажите "
                        + SYSTEM_PROPERTY + ", env " + ENV_VARIABLE
                        + " или ключ " + RESOURCE_KEY + " в classpath:" + CLASSPATH_RESOURCE
        );
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
