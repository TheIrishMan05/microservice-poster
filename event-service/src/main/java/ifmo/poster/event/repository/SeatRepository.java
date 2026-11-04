package ifmo.poster.event.repository;

import ifmo.poster.event.entity.Seat;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface SeatRepository extends R2dbcRepository<Seat, Long> {

    Flux<Seat> findByEventId(Long eventId);

    Flux<Seat> findByEventIdAndTicketTypeId(Long eventId, Long ticketTypeId);

    @Query("""
            SELECT * FROM seats
            WHERE event_id = :eventId
              AND ticket_type_id = :ticketTypeId
              AND ticket_id IS NULL
              AND reservation_id IS NULL
            ORDER BY id ASC
            LIMIT :limit
            FOR UPDATE
            """)
    Flux<Seat> findAvailableForUpdate(Long eventId, Long ticketTypeId, int limit);

    Flux<Seat> findByReservationId(String reservationId);
}
