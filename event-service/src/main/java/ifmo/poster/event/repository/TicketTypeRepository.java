package ifmo.poster.event.repository;

import ifmo.poster.event.entity.TicketType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface TicketTypeRepository extends R2dbcRepository<TicketType, Long> {
    Mono<Boolean> existsByTypeNameIgnoreCase(String typeName);
    Mono<TicketType> findByTypeNameIgnoreCase(String typeName);
    Flux<TicketType> findByEventId(Long eventId);
}
