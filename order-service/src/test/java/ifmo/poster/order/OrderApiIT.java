package ifmo.poster.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.enums.OrderStatus;
import ifmo.poster.contracts.enums.Role;
import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.order.client.EventClient;
import ifmo.poster.order.client.UserClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "30s")
@Testcontainers
class OrderApiIT {

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

    @MockitoBean
    UserClient userClient;

    @MockitoBean
    EventClient eventClient;

    @BeforeEach
    void stubDependencies() {
        when(userClient.getById(1L)).thenReturn(new UserDto(1L, "buyer01", "buyer@example.com", Role.USER));
        when(eventClient.getById(10L)).thenReturn(new EventBriefDto(
                10L,
                "Show",
                "Arena",
                LocalDateTime.now().plusDays(3),
                EventStatus.ACTIVE
        ));
        when(eventClient.reserve(any(ReserveTicketsRequest.class))).thenAnswer(invocation -> {
            ReserveTicketsRequest request = invocation.getArgument(0);
            return new TicketReservationDto(
                    UUID.randomUUID().toString(),
                    request.eventId(),
                    request.ticketTypeId(),
                    request.quantity(),
                    new BigDecimal("2000.00"),
                    List.of(100L, 101L)
            );
        });
    }

    @Test
    void createConfirmAndListOrders() {
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
        assertThat(created.get("status")).isEqualTo(OrderStatus.PENDING.name());

        webTestClient.get()
                .uri("/api/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].tickets.length()").isEqualTo(2);

        webTestClient.put()
                .uri("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", OrderStatus.CONFIRMED.name()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CONFIRMED");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders")
                        .queryParam("userId", 1)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Total-Count")
                .expectBody()
                .jsonPath("$.length()").value(size -> assertThat((Integer) size).isGreaterThanOrEqualTo(1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/tickets")
                        .queryParam("eventId", 10)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Total-Count");
    }

    @Test
    void cancelPendingOrderCompensatesReservation() {
        String reservationId = UUID.randomUUID().toString();
        when(eventClient.reserve(any(ReserveTicketsRequest.class))).thenReturn(new TicketReservationDto(
                reservationId,
                10L,
                1L,
                1,
                new BigDecimal("2000.00"),
                List.of(1L)
        ));

        @SuppressWarnings("unchecked")
        Map<String, Object> created = webTestClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userId", 1,
                        "items", List.of(Map.of(
                                "eventId", 10,
                                "ticketTypeId", 1,
                                "quantity", 1
                        ))
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Long orderId = ((Number) created.get("id")).longValue();

        webTestClient.put()
                .uri("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", OrderStatus.CANCELLED.name()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CANCELLED");

        org.mockito.Mockito.verify(eventClient).cancelReservation(eq(reservationId));
    }
}
