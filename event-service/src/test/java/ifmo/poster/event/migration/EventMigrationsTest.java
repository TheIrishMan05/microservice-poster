package ifmo.poster.event.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class EventMigrationsTest {

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:15-alpine");

    @Test
    void appliesAllMigrationsAndCreatesOnePrimaryKeyForEventTags() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        liquibase.afterPropertiesSet();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            assertThat(constraintDefinition(statement, "event_tags_pkey"))
                    .isEqualTo("PRIMARY KEY (id)");
            assertThat(constraintDefinition(statement, "uq_event_tag_pair"))
                    .isEqualTo("UNIQUE (event_id, tag_id)");
        }
    }

    private String constraintDefinition(Statement statement, String constraintName) throws Exception {
        String query = """
                SELECT pg_get_constraintdef(oid)
                FROM pg_constraint
                WHERE conname = '%s'
                """.formatted(constraintName);
        try (ResultSet result = statement.executeQuery(query)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
