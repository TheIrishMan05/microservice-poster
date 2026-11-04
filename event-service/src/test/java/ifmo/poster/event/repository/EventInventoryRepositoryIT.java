package ifmo.poster.event.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.event.entity.Event;
import ifmo.poster.event.entity.Seat;
import ifmo.poster.event.entity.TicketType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class EventInventoryRepositoryIT {

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
    EventRepository eventRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    TicketTypeRepository ticketTypeRepository;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Test
    void findNextBatchOfEventsUsesCursorAndStatusFilter() {
        Event pending = saveEvent("Pending Show", EventStatus.PENDING);
        Event activeOld = saveEvent("Active Old", EventStatus.ACTIVE);
        Event activeNew = saveEvent("Active New", EventStatus.ACTIVE);

        StepVerifier.create(eventRepository.findNextBatchOfEvents(
                        EventStatus.ACTIVE.name(),
                        activeOld.getId(),
                        10
                ).collectList())
                .assertNext(batch -> {
                    assertThat(batch).extracting(Event::getId).containsExactly(activeNew.getId());
                    assertThat(batch).noneMatch(event -> event.getId().equals(pending.getId()));
                })
                .verifyComplete();
    }

    @Test
    void findAvailableForUpdateReturnsOnlyFreeSeatsWithinLimit() {
        Event event = saveEvent("Seat Map", EventStatus.ACTIVE);
        TicketType vip = ticketTypeRepository.findByTypeNameIgnoreCase("VIP").block();
        assertThat(vip).isNotNull();

        Seat free1 = saveSeat(event.getId(), vip.getId(), 1, null);
        Seat free2 = saveSeat(event.getId(), vip.getId(), 2, null);
        Seat taken = saveSeat(event.getId(), vip.getId(), 3, "already-taken");
        saveSeat(event.getId(), vip.getId(), 4, null);

        StepVerifier.create(
                        transactionalOperator.transactional(
                                seatRepository.findAvailableForUpdate(event.getId(), vip.getId(), 2).collectList()
                        )
                )
                .assertNext(available -> {
                    assertThat(available).hasSize(2);
                    assertThat(available).extracting(Seat::getId)
                            .containsExactly(free1.getId(), free2.getId())
                            .doesNotContain(taken.getId());
                })
                .verifyComplete();

        free1.setReservationId("hold-1");
        seatRepository.save(free1).block();

        StepVerifier.create(
                        transactionalOperator.transactional(
                                seatRepository.findAvailableForUpdate(event.getId(), vip.getId(), 10)
                                        .map(Seat::getId)
                                        .collectList()
                        )
                )
                .assertNext(ids -> assertThat(ids).doesNotContain(free1.getId(), taken.getId()))
                .verifyComplete();
    }

    private Event saveEvent(String name, EventStatus status) {
        Event event = new Event();
        event.setEventName(name);
        event.setDescription("repo-it");
        event.setLocation("Hall");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setStatus(status);
        return eventRepository.save(event).block();
    }

    private Seat saveSeat(Long eventId, Long ticketTypeId, int number, String reservationId) {
        Seat seat = new Seat();
        seat.setEventId(eventId);
        seat.setTicketTypeId(ticketTypeId);
        seat.setNumber(number);
        seat.setRow("A");
        seat.setReservationId(reservationId);
        return seatRepository.save(seat).block();
    }
}
