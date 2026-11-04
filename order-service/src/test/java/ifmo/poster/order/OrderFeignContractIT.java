package ifmo.poster.order;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.contracts.enums.OrderStatus;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
class OrderFeignContractIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:15-alpine");

    static MockWebServer userService;
    static MockWebServer eventService;

    @BeforeAll
    static void startServers() throws IOException {
        userService = new MockWebServer();
        eventService = new MockWebServer();
        userService.start();
        eventService.start();
    }

    @AfterAll
    static void stopServers() throws IOException {
        userService.shutdown();
        eventService.shutdown();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("clients.user-service.url", () -> userService.url("/").toString().replaceAll("/$", ""));
        registry.add("clients.event-service.url", () -> eventService.url("/").toString().replaceAll("/$", ""));
        registry.add("resilience4j.retry.instances.userService.maxAttempts", () -> "1");
        registry.add("resilience4j.retry.instances.eventService.maxAttempts", () -> "1");
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    void createAndCancelOrderUsesFeignContractsAgainstDownstreamServices() throws Exception {
        String reservationId = UUID.randomUUID().toString();

        userService.enqueue(jsonResponse(200, """
                {
                  "id": 1,
                  "userName": "buyer01",
                  "email": "buyer@example.com",
                  "role": "USER"
                }
                """));
        eventService.enqueue(jsonResponse(200, """
                {
                  "id": 10,
                  "name": "Show",
                  "location": "Arena",
                  "dateTime": "2030-01-15T19:00:00",
                  "status": "ACTIVE"
                }
                """));
        eventService.enqueue(jsonResponse(201, """
                {
                  "reservationId": "%s",
                  "eventId": 10,
                  "ticketTypeId": 1,
                  "quantity": 2,
                  "unitPrice": 2000.00,
                  "ticketIds": [100, 101]
                }
                """.formatted(reservationId)));
        eventService.enqueue(new MockResponse().setResponseCode(204));

        @SuppressWarnings("unchecked")
        Map<String, Object> created = webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userId", 1,
                        "items", List.of(Map.of(
                                "eventId", 10,
                                "ticketTypeId", 1,
                                "quantity", 2
                        ))
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        Long orderId = ((Number) created.get("id")).longValue();

        RecordedRequest userRequest = userService.takeRequest(5, TimeUnit.SECONDS);
        assertThat(userRequest).isNotNull();
        assertThat(userRequest.getMethod()).isEqualTo("GET");
        assertThat(userRequest.getPath()).isEqualTo("/internal/users/1");

        RecordedRequest eventGet = eventService.takeRequest(5, TimeUnit.SECONDS);
        assertThat(eventGet).isNotNull();
        assertThat(eventGet.getPath()).isEqualTo("/internal/events/10");

        RecordedRequest reserve = eventService.takeRequest(5, TimeUnit.SECONDS);
        assertThat(reserve).isNotNull();
        assertThat(reserve.getMethod()).isEqualTo("POST");
        assertThat(reserve.getPath()).isEqualTo("/internal/events/reservations");
        assertThat(reserve.getBody().readUtf8()).contains("\"eventId\":10").contains("\"quantity\":2");

        webTestClient.put()
                .uri("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", OrderStatus.CANCELLED.name()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CANCELLED");

        RecordedRequest cancel = eventService.takeRequest(5, TimeUnit.SECONDS);
        assertThat(cancel).isNotNull();
        assertThat(cancel.getMethod()).isEqualTo("DELETE");
        assertThat(cancel.getPath()).isEqualTo("/internal/events/reservations/" + reservationId);
    }

    private static MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
