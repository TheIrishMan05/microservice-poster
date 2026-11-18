package ifmo.poster.contracts.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PostgresTestImageTest {

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty(PostgresTestImage.SYSTEM_PROPERTY);
    }

    @Test
    void prefersSystemProperty() {
        System.setProperty(PostgresTestImage.SYSTEM_PROPERTY, "postgres:16-alpine");
        assertThat(PostgresTestImage.resolve()).isEqualTo("postgres:16-alpine");
    }
}
