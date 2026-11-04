package ifmo.poster.event.repository;

import ifmo.poster.event.entity.TicketReservation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface TicketReservationRepository extends R2dbcRepository<TicketReservation, Long> {

    Mono<TicketReservation> findByPublicId(String publicId);
}
