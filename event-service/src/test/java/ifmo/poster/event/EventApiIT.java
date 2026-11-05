package ifmo.poster.event;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.contracts.enums.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
class EventApiIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:15-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://%s:%d/%s".formatted(
                        POSTGRES.getHost(),
                        POSTGRES.getFirstMappedPort(),
                        POSTGRES.getDatabaseName()
                ));
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        registry.add("spring.liquibase.user", POSTGRES::getUsername);
        registry.add("spring.liquibase.password", POSTGRES::getPassword);
    }

    @Autowired
    WebTestClient webTestClient;

    @Test
    void eventCrudWithCursorPaginationAndTags() {
        Long eventId = createEvent("Concert", EventStatus.PENDING, List.of("music", "live"));

        webTestClient.get()
                .uri("/api/events/{id}", eventId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.eventName").isEqualTo("Concert")
                .jsonPath("$.tags.length()").isEqualTo(2);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/events")
                        .queryParam("afterId", 0)
                        .queryParam("limit", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].id").exists();

        webTestClient.put()
                .uri("/api/events/{id}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "eventName", "Concert Updated",
                        "description", "Updated description",
                        "location", "Hall 2",
                        "dateTime", LocalDateTime.now().plusDays(2).toString(),
                        "status", EventStatus.ACTIVE.name(),
                        "tagNames", List.of("music")
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.eventName").isEqualTo("Concert Updated")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.tags.length()").isEqualTo(1);

        webTestClient.delete()
                .uri("/api/events/{id}", eventId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void reserveAndCancelTicketsForActiveEvent() {
        Long eventId = createEvent("Festival", EventStatus.ACTIVE, List.of("fest"));
        // VIP уже создан Liquibase seed (004), повторный POST даёт 409
        Long ticketTypeId = ticketTypeIdByName("VIP");

        @SuppressWarnings("unchecked")
        Map<String, Object> reservation = webTestClient.post()
                .uri("/internal/events/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "eventId", eventId,
                        "ticketTypeId", ticketTypeId,
                        "quantity", 2
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(reservation).isNotNull();
        String reservationId = (String) reservation.get("reservationId");
        assertThat(reservationId).isNotBlank();
        assertThat((List<?>) reservation.get("ticketIds")).hasSize(2);

        webTestClient.get()
                .uri("/internal/events/{id}", eventId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Festival");

        webTestClient.delete()
                .uri("/internal/events/reservations/{reservationId}", reservationId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void registrationListExposesTotalCount() {
        Long eventId = createEvent("Meetup", EventStatus.ACTIVE, List.of());

        @SuppressWarnings("unchecked")
        Map<String, Object> registration = webTestClient.post()
                .uri("/api/registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userId", 42,
                        "eventId", eventId,
                        "status", "CONFIRMED"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(registration).isNotNull();
        Long registrationId = ((Number) registration.get("id")).longValue();

        webTestClient.get()
                .uri("/api/registrations/{id}", registrationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(42);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/registrations")
                        .queryParam("eventId", eventId)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1");

        webTestClient.put()
                .uri("/api/registrations/{id}", registrationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "CANCELED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CANCELED");

        webTestClient.delete()
                .uri("/api/registrations/{id}", registrationId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void tagAndTicketTypeCrud() {
        @SuppressWarnings("unchecked")
        Map<String, Object> tag = webTestClient.post()
                .uri("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "jazz"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(tag).isNotNull();
        Long tagId = ((Number) tag.get("id")).longValue();

        webTestClient.get()
                .uri("/api/tags/{id}", tagId)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/tags").queryParam("page", 0).queryParam("size", 10).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();

        webTestClient.put()
                .uri("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "blues"))
                .exchange()
                .expectStatus().isOk();

        Long ticketTypeId = createTicketType("EARLY_BIRD");
        webTestClient.get()
                .uri("/api/ticket-types/{id}", ticketTypeId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price").exists();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket-types").queryParam("page", 0).queryParam("size", 10).build())
                .exchange()
                .expectStatus().isOk();

        webTestClient.put()
                .uri("/api/ticket-types/{id}", ticketTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "typeName", "EARLY_BIRD_PLUS",
                        "price", new BigDecimal("999.00")
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.typeName").isEqualTo("EARLY_BIRD_PLUS");

        webTestClient.delete()
                .uri("/api/ticket-types/{id}", ticketTypeId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete()
                .uri("/api/tags/{id}", tagId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void waitlistCrudAndNotFoundPaths() {
        Long eventId = createEvent("Waitlist Night", EventStatus.ACTIVE, List.of("queue"));
        Long ticketTypeId = ticketTypeIdByName("STANDARD");

        @SuppressWarnings("unchecked")
        Map<String, Object> waitlist = webTestClient.post()
                .uri("/api/ticket-waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "userId", 7,
                        "eventId", eventId,
                        "ticketTypeId", ticketTypeId,
                        "status", "WAITING"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(waitlist).isNotNull();
        Long waitlistId = ((Number) waitlist.get("id")).longValue();

        webTestClient.get()
                .uri("/api/ticket-waitlist/{id}", waitlistId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("WAITING");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ticket-waitlist")
                        .queryParam("eventId", eventId)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Total-Count");

        webTestClient.put()
                .uri("/api/ticket-waitlist/{id}", waitlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "FULFILLED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FULFILLED");

        webTestClient.delete()
                .uri("/api/ticket-waitlist/{id}", waitlistId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/events/{id}", 999_999)
                .exchange()
                .expectStatus().isNotFound();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/events")
                        .queryParam("afterId", 0)
                        .queryParam("limit", 5)
                        .queryParam("status", EventStatus.ACTIVE.name())
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void bulkCreateEventsAndClearDatabase() {
        webTestClient.post()
                .uri("/api/events/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "events", List.of(
                                Map.of(
                                        "eventName", "Bulk Event A",
                                        "description", "Bulk A",
                                        "location", "Hall A",
                                        "dateTime", LocalDateTime.now().plusDays(1).toString(),
                                        "status", EventStatus.PENDING.name(),
                                        "tagNames", List.of("bulk")
                                ),
                                Map.of(
                                        "eventName", "Bulk Event B",
                                        "description", "Bulk B",
                                        "location", "Hall B",
                                        "dateTime", LocalDateTime.now().plusDays(2).toString(),
                                        "status", EventStatus.ACTIVE.name(),
                                        "tagNames", List.of("bulk")
                                )
                        )
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].eventName").isEqualTo("Bulk Event A")
                .jsonPath("$[1].eventName").isEqualTo("Bulk Event B");

        webTestClient.delete()
                .uri("/api/events/data")
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/events")
                        .queryParam("afterId", 0)
                        .queryParam("limit", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);

        // восстанавливаем справочники из seed — другие тесты класса на них завязаны
        createTicketType("VIP");
        createTicketType("STANDARD");
    }

    @Test
    void rejectsEmptyBulkCreateEvents() {
        webTestClient.post()
                .uri("/api/events/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("events", List.of()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @SuppressWarnings("unchecked")
    private Long createEvent(String name, EventStatus status, List<String> tags) {
        Map<String, Object> created = webTestClient.post()
                .uri("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "eventName", name,
                        "description", "Description for " + name,
                        "location", "Main Arena",
                        "dateTime", LocalDateTime.now().plusDays(1).toString(),
                        "status", status.name(),
                        "tagNames", tags
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        return ((Number) created.get("id")).longValue();
    }

    @SuppressWarnings("unchecked")
    private Long ticketTypeIdByName(String typeName) {
        List<Map<String, Object>> ticketTypes = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/ticket-types").queryParam("page", 0).queryParam("size", 20).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(ticketTypes).isNotEmpty();
        return ticketTypes.stream()
                .filter(item -> typeName.equals(item.get("typeName")))
                .map(item -> ((Number) item.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ticket type not found: " + typeName));
    }

    @SuppressWarnings("unchecked")
    private Long createTicketType(String typeName) {
        Map<String, Object> created = webTestClient.post()
                .uri("/api/ticket-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "typeName", typeName,
                        "price", new BigDecimal("1500.00")
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
