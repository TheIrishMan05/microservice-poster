package ifmo.poster.event.repository;

import ifmo.poster.event.entity.TicketReservationHold;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface TicketReservationHoldRepository extends R2dbcRepository<TicketReservationHold, Long> {

    Flux<TicketReservationHold> findByReservationId(Long reservationId);
}
