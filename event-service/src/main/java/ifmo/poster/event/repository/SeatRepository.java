package ifmo.poster.event.repository;

import ifmo.poster.event.entity.Seat;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface SeatRepository extends R2dbcRepository<Seat, Long> {
    Flux<Seat> findByTicketTypeId(Long ticketTypeId);
    Flux<Seat> findByEventId(Long eventId);
}
