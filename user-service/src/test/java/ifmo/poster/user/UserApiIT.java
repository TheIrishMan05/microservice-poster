package ifmo.poster.user;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.contracts.enums.Role;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "30s")
@Testcontainers
class UserApiIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:15-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    void userCrudAndInternalEndpoint() {
        Long userId = createUser("alice01", "alice@example.com");

        webTestClient.get()
                .uri("/api/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userName").isEqualTo("alice01");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/users").queryParam("page", 0).queryParam("size", 20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();

        webTestClient.get()
                .uri("/internal/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userName").isEqualTo("alice01");

        webTestClient.put()
                .uri("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userName", "alice02",
                        "email", "alice2@example.com",
                        "role", Role.ADMIN.name()
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userName").isEqualTo("alice02")
                .jsonPath("$.role").isEqualTo("ADMIN");

        webTestClient.delete()
                .uri("/api/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/users/{id}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void notificationCrudExposesTotalCountHeader() {
        Long userId = createUser("notify_user", "notify@example.com");

        webTestClient.post()
                .uri("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userId", userId,
                        "title", "Hello",
                        "message", "World"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Hello");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/notifications")
                        .queryParam("userId", userId)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void rejectsInvalidUserPayload() {
        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userName", "ab",
                        "email", "not-an-email",
                        "role", Role.USER.name()
                ))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void rejectsDuplicateUsername() {
        createUser("unique_name", "first@example.com");

        webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userName", "unique_name",
                        "email", "second@example.com",
                        "role", Role.USER.name()
                ))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @SuppressWarnings("unchecked")
    private Long createUser(String userName, String email) {
        Map<String, Object> created = webTestClient.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userName", userName,
                        "email", email,
                        "role", Role.USER.name()
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        return ((Number) created.get("id")).longValue();
    }
}
